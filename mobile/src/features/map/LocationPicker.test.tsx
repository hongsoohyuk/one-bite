import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '../../shared/i18n'; // react-i18next 인스턴스 초기화 (ko) → t() 가 실제 문구 반환

vi.mock('./kakaoLoader', () => ({ loadKakaoMaps: vi.fn() }));

import { loadKakaoMaps } from './kakaoLoader';
import { LocationPicker, type SelectedPlace } from './LocationPicker';

const loadMock = loadKakaoMaps as unknown as ReturnType<typeof vi.fn>;
const CENTER = { lat: 37.5, lng: 127 };

const GS25 = {
  id: '1',
  place_name: 'GS25 신용산점',
  address_name: '서울 용산구 한강로동',
  road_address_name: '서울 용산구 한강대로 23',
  x: '126.96',
  y: '37.52',
  distance: '120',
};

// 가짜 maps SDK: 마커 클릭 핸들러/검색 옵션을 수집해 위치기반 검색·선택을 검증한다.
function makeFakeMaps(keywordResult: { data: unknown[]; status: string }) {
  const clickHandlers: Array<(e?: unknown) => void> = [];
  let capturedOptions: Record<string, unknown> | undefined;
  const latlng = (lat: number, lng: number) => ({ getLat: () => lat, getLng: () => lng });
  const mapInstance = {
    setCenter: vi.fn(),
    getCenter: vi.fn(() => latlng(37.5, 127)),
    setLevel: vi.fn(),
    setBounds: vi.fn(),
  };
  const fakeMaps = {
    load: (cb: () => void) => cb(),
    LatLng: vi.fn(function (this: unknown, lat: number, lng: number) {
      return latlng(lat, lng);
    }),
    LatLngBounds: vi.fn(function (this: unknown) {
      return { extend: vi.fn() };
    }),
    Map: vi.fn(function (this: unknown) {
      return mapInstance;
    }),
    Marker: vi.fn(function (this: unknown) {
      return { setPosition: vi.fn(), setMap: vi.fn(), getPosition: () => latlng(0, 0) };
    }),
    event: {
      addListener: vi.fn((_t: object, type: string, h: (e?: unknown) => void) => {
        if (type === 'click') clickHandlers.push(h);
      }),
    },
    services: {
      Status: { OK: 'OK', ZERO_RESULT: 'ZERO_RESULT', ERROR: 'ERROR' },
      SortBy: { ACCURACY: 'accuracy', DISTANCE: 'distance' },
      Places: vi.fn(function (this: unknown) {
        return {
          keywordSearch: (
            _q: string,
            cb: (d: unknown[], s: string) => void,
            options?: Record<string, unknown>,
          ) => {
            capturedOptions = options;
            cb(keywordResult.data, keywordResult.status);
          },
        };
      }),
    },
  };
  return { fakeMaps, clickHandlers, mapInstance, getOptions: () => capturedOptions };
}

describe('LocationPicker', () => {
  beforeEach(() => loadMock.mockReset());

  it('SDK 사용 불가 시 안내 문구로 graceful degrade', async () => {
    loadMock.mockResolvedValue(null);
    const onUnavailable = vi.fn();
    render(
      <LocationPicker
        center={CENTER}
        selected={null}
        onSelect={vi.fn()}
        onUnavailable={onUnavailable}
      />,
    );
    expect(
      await screen.findByText('지도를 불러올 수 없어요. 아래 상세 위치를 입력해 주세요'),
    ).toBeInTheDocument();
    expect(onUnavailable).toHaveBeenCalled();
  });

  it('현재 위치 기준(거리순) 검색 → 결과 클릭 시 onSelect(점포명+도로명+좌표)', async () => {
    const { fakeMaps, getOptions } = makeFakeMaps({ status: 'OK', data: [GS25] });
    loadMock.mockResolvedValue(fakeMaps);
    const onSelect = vi.fn();
    render(<LocationPicker center={CENTER} selected={null} onSelect={onSelect} />);
    await waitFor(() => expect(fakeMaps.Map).toHaveBeenCalled());

    await userEvent.type(screen.getByLabelText('점포 검색'), 'GS25');
    await userEvent.keyboard('{Enter}');

    // 위치 기반(반경 + 거리순) 옵션이 들어갔는지 검증
    expect(getOptions()).toMatchObject({ radius: 20000, sort: 'distance' });
    expect(getOptions()?.location).toBeTruthy();

    const result = await screen.findByRole('button', { name: /GS25 신용산점/ });
    await userEvent.click(result);

    expect(onSelect).toHaveBeenCalledWith({
      placeName: 'GS25 신용산점',
      roadAddress: '서울 용산구 한강대로 23',
      coords: { lat: 37.52, lng: 126.96 },
    });
  });

  it('지도 마커 클릭으로도 점포가 선택된다', async () => {
    const { fakeMaps, clickHandlers } = makeFakeMaps({ status: 'OK', data: [GS25] });
    loadMock.mockResolvedValue(fakeMaps);
    const onSelect = vi.fn();
    render(<LocationPicker center={CENTER} selected={null} onSelect={onSelect} />);
    await waitFor(() => expect(fakeMaps.Map).toHaveBeenCalled());

    await userEvent.type(screen.getByLabelText('점포 검색'), 'GS25');
    await userEvent.keyboard('{Enter}');

    // 결과 1건 → 마커 1개의 click 핸들러가 수집됨
    expect(clickHandlers).toHaveLength(1);
    await act(async () => clickHandlers[0]());

    expect(onSelect).toHaveBeenCalledWith(
      expect.objectContaining({ placeName: 'GS25 신용산점', coords: { lat: 37.52, lng: 126.96 } }),
    );
  });

  it('결과 없음 시 안내 문구', async () => {
    const { fakeMaps } = makeFakeMaps({ status: 'ZERO_RESULT', data: [] });
    loadMock.mockResolvedValue(fakeMaps);
    render(<LocationPicker center={CENTER} selected={null} onSelect={vi.fn()} />);
    await waitFor(() => expect(fakeMaps.Map).toHaveBeenCalled());
    await userEvent.type(screen.getByLabelText('점포 검색'), 'zzz');
    await userEvent.keyboard('{Enter}');
    expect(await screen.findByText('검색 결과가 없어요')).toBeInTheDocument();
  });

  it('선택된 점포는 카드로 보이고, 검색창은 숨고, 변경 시 onSelect(null)', async () => {
    const { fakeMaps } = makeFakeMaps({ status: 'ZERO_RESULT', data: [] });
    loadMock.mockResolvedValue(fakeMaps);
    const onSelect = vi.fn();
    const selected: SelectedPlace = {
      placeName: 'GS25 신용산점',
      roadAddress: '서울 용산구 한강대로 23',
      coords: { lat: 37.52, lng: 126.96 },
    };
    render(<LocationPicker center={CENTER} selected={selected} onSelect={onSelect} />);
    await waitFor(() => expect(fakeMaps.Map).toHaveBeenCalled());

    expect(screen.getByText('GS25 신용산점')).toBeInTheDocument();
    // 점포가 정해졌으면 검색 입력은 노출되지 않음 (재선택은 "변경"으로)
    expect(screen.queryByLabelText('점포 검색')).toBeNull();

    await userEvent.click(screen.getByRole('button', { name: '변경' }));
    expect(onSelect).toHaveBeenCalledWith(null);
  });
});
