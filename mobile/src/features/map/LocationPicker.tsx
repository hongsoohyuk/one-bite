import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { MapPin, Search, Loader2, X } from 'lucide-react';
import {
  loadKakaoMaps,
  type KakaoMaps,
  type KakaoMapInstance,
  type KakaoMarker,
  type KakaoPlace,
} from './kakaoLoader';
import { type Coords } from '../../shared/stores/locationStore';

// 검색해서 고른 "실제 점포" 1건. 좌표는 점포의 정확한 위치(직접 찍지 않음).
export type SelectedPlace = {
  placeName: string;
  roadAddress: string;
  coords: Coords;
};

type LocationPickerProps = {
  /** 검색 기준 + 지도 초기 중심 (보통 현재 GPS). */
  center: Coords;
  /** 현재 선택된 점포 (없으면 검색 UI 노출). 상위가 보관. */
  selected: SelectedPlace | null;
  /** 점포를 고르면 호출. "변경"으로 비우면 null. */
  onSelect: (place: SelectedPlace | null) => void;
  /** SDK/키 사용 불가 판정 시 호출 → 상위에서 상세-only 폴백 허용. */
  onUnavailable?: () => void;
};

// 검색 반경(미터). 거리순 정렬과 함께 "현재 위치 주변"을 만든다. Kakao 최대 20km.
const SEARCH_RADIUS_M = 20000;

/**
 * 만날 점포 선택기: 카카오 장소 검색을 현재 위치 기준(거리순)으로 돌려
 * 실제 존재하는 점포만 고르도록 강제한다. 결과는 리스트 + 지도 마커로 보여주고,
 * 둘 중 어느 쪽을 눌러도 선택된다. 상세 위치(자유 입력)는 상위 화면이 담당.
 * 키/SDK 없으면 graceful degrade (검색·지도 숨김 → 상위는 상세-only 등록 허용).
 */
export function LocationPicker({ center, selected, onSelect, onUnavailable }: LocationPickerProps) {
  const { t } = useTranslation();
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapsRef = useRef<KakaoMaps | null>(null);
  const mapRef = useRef<KakaoMapInstance | null>(null);
  // 검색 결과 마커들 / 선택된 점포 마커. 재검색·선택 시 갈아끼우기 위해 ref 로 보관.
  const resultMarkersRef = useRef<KakaoMarker[]>([]);
  const selectedMarkerRef = useRef<KakaoMarker | null>(null);
  // effect 안 리스너에서 최신 onSelect 를 쓰기 위해 ref 에 보관.
  const onSelectRef = useRef(onSelect);
  useEffect(() => {
    onSelectRef.current = onSelect;
  }, [onSelect]);

  const [failed, setFailed] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState<KakaoPlace[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const clearResultMarkers = () => {
    for (const m of resultMarkersRef.current) m.setMap(null);
    resultMarkersRef.current = [];
  };
  const clearSelectedMarker = () => {
    selectedMarkerRef.current?.setMap(null);
    selectedMarkerRef.current = null;
  };

  // 좌표에 선택 마커를 찍고 그 위치로 확대 이동.
  const focusSelected = (coords: Coords) => {
    const maps = mapsRef.current;
    const map = mapRef.current;
    if (!maps || !map) return;
    clearResultMarkers();
    clearSelectedMarker();
    const latlng = new maps.LatLng(coords.lat, coords.lng);
    selectedMarkerRef.current = new maps.Marker({ position: latlng, map, zIndex: 5 });
    map.setCenter(latlng);
    map.setLevel(3);
  };

  // 지도 초기화 (마운트 시 1회). 이미 고른 점포가 있으면 그 위치를 비춘다.
  useEffect(() => {
    let cancelled = false;
    void (async () => {
      const maps = await loadKakaoMaps();
      if (cancelled) return;
      const el = containerRef.current;
      if (!maps || !el) {
        setFailed(true);
        onUnavailable?.();
        return;
      }
      mapsRef.current = maps;
      const start = selected?.coords ?? center;
      const map = new maps.Map(el, {
        center: new maps.LatLng(start.lat, start.lng),
        level: selected ? 3 : 4,
      });
      mapRef.current = map;
      if (selected) focusSelected(selected.coords);
    })();
    return () => {
      cancelled = true;
    };
    // center/selected 는 의도적으로 1회만 (이후엔 검색/선택이 지도를 움직임)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 선택된 점포를 지도에 마커로 그린다.
  const renderResultMarkers = (places: KakaoPlace[]) => {
    const maps = mapsRef.current;
    const map = mapRef.current;
    if (!maps || !map) return;
    clearSelectedMarker();
    clearResultMarkers();
    const bounds = new maps.LatLngBounds();
    for (const place of places) {
      const latlng = new maps.LatLng(Number(place.y), Number(place.x));
      const marker = new maps.Marker({ position: latlng, map });
      maps.event.addListener(marker, 'click', () => pickPlace(place));
      resultMarkersRef.current.push(marker);
      bounds.extend(latlng);
    }
    if (places.length > 0) map.setBounds(bounds);
  };

  const runSearch = () => {
    const maps = mapsRef.current;
    const map = mapRef.current;
    const q = keyword.trim();
    if (!maps || q === '') return;
    setSearching(true);
    setSearchError(null);
    const places = new maps.services.Places();
    // 현재 보고 있는 지도 중심 기준 거리순 → "근처 GS25" 같은 검색이 의도대로 동작.
    const origin = map?.getCenter() ?? new maps.LatLng(center.lat, center.lng);
    places.keywordSearch(
      q,
      (data, status) => {
        setSearching(false);
        if (status === maps.services.Status.OK) {
          setResults(data);
          renderResultMarkers(data);
        } else if (status === maps.services.Status.ZERO_RESULT) {
          setResults([]);
          renderResultMarkers([]);
          setSearchError(t('create.locationNoResult'));
        } else {
          setResults([]);
          setSearchError(t('create.locationSearchError'));
        }
      },
      { location: origin, radius: SEARCH_RADIUS_M, sort: maps.services.SortBy.DISTANCE, size: 15 },
    );
  };

  const pickPlace = (place: KakaoPlace) => {
    const coords: Coords = { lat: Number(place.y), lng: Number(place.x) };
    onSelectRef.current({
      placeName: place.place_name,
      roadAddress: place.road_address_name || place.address_name,
      coords,
    });
    focusSelected(coords);
    setResults([]);
    setSearchError(null);
    setKeyword('');
  };

  const clearSelection = () => {
    clearSelectedMarker();
    onSelect(null);
    setKeyword('');
    setResults([]);
    setSearchError(null);
  };

  if (failed) {
    return (
      <div className="flex flex-col gap-1">
        <span className="text-meta text-gray-500 dark:text-gray-400">
          {t('create.locationLabel')}
        </span>
        <div className="flex items-center gap-2 rounded-sm border border-gray-200 bg-gray-50 px-4 py-3 dark:border-gray-700 dark:bg-gray-900">
          <MapPin className="size-5 shrink-0 text-gray-400" aria-hidden />
          <p className="text-caption text-gray-500 dark:text-gray-400">
            {t('create.locationUnavailable')}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-1">
      <span className="text-meta text-gray-500 dark:text-gray-400">
        {t('create.locationLabel')}
      </span>

      {selected ? (
        /* 선택된 점포 카드 (검색 UI 대신 노출, "변경"으로 다시 검색) */
        <div className="flex items-center gap-2 rounded-sm border border-brand bg-brand-surface px-4 py-3 dark:border-brand-dark-adj dark:bg-brand-surface-dark">
          <MapPin className="size-5 shrink-0 text-brand dark:text-brand-dark-adj" aria-hidden />
          <div className="flex min-w-0 flex-1 flex-col">
            <span className="truncate text-body text-gray-900 dark:text-gray-100">
              {selected.placeName}
            </span>
            {selected.roadAddress && (
              <span className="truncate text-caption text-gray-500 dark:text-gray-400">
                {selected.roadAddress}
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={clearSelection}
            aria-label={t('create.locationChange')}
            className="flex shrink-0 items-center gap-1 text-caption text-gray-500 dark:text-gray-400"
          >
            <X className="size-4" aria-hidden />
            {t('create.locationChange')}
          </button>
        </div>
      ) : (
        <>
          <p className="text-caption text-gray-400">{t('create.locationSearchHint')}</p>

          {/* 장소 검색 박스 */}
          <div className="flex h-[52px] items-center rounded-sm border border-gray-200 bg-white px-4 transition-colors focus-within:border-brand focus-within:ring-1 focus-within:ring-brand dark:border-gray-700 dark:bg-gray-900">
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  runSearch();
                }
              }}
              aria-label={t('create.locationSearchLabel')}
              placeholder={t('create.locationSearchPlaceholder')}
              className="flex-1 bg-transparent text-body text-gray-900 outline-none placeholder:text-gray-400 dark:text-gray-100"
            />
            <button
              type="button"
              onClick={runSearch}
              aria-label={t('create.locationSearchAction')}
              className="ml-2 flex items-center text-gray-400"
            >
              {searching ? (
                <Loader2 className="size-5 animate-spin" aria-hidden />
              ) : (
                <Search className="size-5" aria-hidden />
              )}
            </button>
          </div>

          {searchError && (
            <p className="text-caption text-gray-500 dark:text-gray-400">{searchError}</p>
          )}

          {/* 검색 결과 리스트 (거리 표시) */}
          {results.length > 0 && (
            <ul className="flex max-h-60 flex-col divide-y divide-gray-100 overflow-y-auto rounded-sm border border-gray-200 dark:divide-gray-800 dark:border-gray-700">
              {results.map((place) => (
                <li key={place.id}>
                  <button
                    type="button"
                    onClick={() => pickPlace(place)}
                    className="flex w-full items-start gap-2 px-4 py-3 text-left hover:bg-gray-50 dark:hover:bg-gray-800"
                  >
                    <div className="flex min-w-0 flex-1 flex-col gap-0.5">
                      <span className="truncate text-body text-gray-900 dark:text-gray-100">
                        {place.place_name}
                      </span>
                      <span className="truncate text-caption text-gray-500 dark:text-gray-400">
                        {place.road_address_name || place.address_name}
                      </span>
                    </div>
                    {place.distance && (
                      <span className="shrink-0 text-caption text-brand dark:text-brand-dark-adj">
                        {formatDistance(place.distance)}
                      </span>
                    )}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </>
      )}

      {/* 지도: 검색 결과 핀 + 선택 핀을 보여줌 */}
      <div
        ref={containerRef}
        data-testid="location-picker-map"
        className="mt-1 h-44 w-full overflow-hidden rounded-sm border border-gray-200 dark:border-gray-700"
      />
      <p className="text-caption text-gray-400">
        {selected ? t('create.locationPickedHint') : t('create.locationResultHint')}
      </p>
    </div>
  );
}

// "1234"(m) → "1.2km" / "120m". 카카오는 미터 문자열을 준다.
function formatDistance(distance: string): string {
  const m = Number(distance);
  if (!Number.isFinite(m)) return '';
  return m >= 1000 ? `${(m / 1000).toFixed(1)}km` : `${m}m`;
}
