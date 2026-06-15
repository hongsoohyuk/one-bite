import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import '../shared/i18n'; // react-i18next 인스턴스 초기화 (ko) → 상세 위치 라벨 등 실제 문구

const mutate = vi.fn();
vi.mock('../features/splits/queries', () => ({ useCreateSplit: vi.fn() }));
vi.mock('../features/upload/imagePicker', () => ({ pickImage: vi.fn() }));
vi.mock('../features/upload/uploadImage', () => ({ uploadImage: vi.fn() }));

// LocationPicker 는 카카오 SDK 의존 → 점포 선택/지도 불가를 stub 버튼으로 노출.
// "장소선택": 코스트코 양재점(점포명+도로명+좌표) 선택. "지도불가": SDK 폴백(onUnavailable).
vi.mock('../features/map/LocationPicker', () => ({
  LocationPicker: ({
    onSelect,
    onUnavailable,
  }: {
    onSelect: (
      p: { placeName: string; roadAddress: string; coords: { lat: number; lng: number } } | null,
    ) => void;
    onUnavailable?: () => void;
  }) => (
    <div>
      <button
        onClick={() =>
          onSelect({
            placeName: '코스트코 양재점',
            roadAddress: '서울 서초구 양재대로 79',
            coords: { lat: 37.47, lng: 127.04 },
          })
        }
      >
        장소선택
      </button>
      <button onClick={() => onUnavailable?.()}>지도불가</button>
    </div>
  ),
}));

import { useCreateSplit } from '../features/splits/queries';
import { pickImage } from '../features/upload/imagePicker';
import { uploadImage } from '../features/upload/uploadImage';
import { CreateSplit } from './CreateSplit';

const useCreateSplitMock = useCreateSplit as unknown as ReturnType<typeof vi.fn>;
const pickImageMock = pickImage as unknown as ReturnType<typeof vi.fn>;
const uploadImageMock = uploadImage as unknown as ReturnType<typeof vi.fn>;

function renderCreate() {
  return render(
    <MemoryRouter>
      <CreateSplit />
    </MemoryRouter>,
  );
}

async function fillProductFields() {
  await userEvent.type(screen.getByLabelText('상품명'), '두쫀쿠');
  await userEvent.type(screen.getByLabelText('전체 가격'), '20000');
  await userEvent.type(screen.getByLabelText('전체 수량'), '4');
  await userEvent.type(screen.getByLabelText('나눌 인원'), '2');
}

describe('CreateSplit', () => {
  beforeEach(() => {
    mutate.mockReset();
    pickImageMock.mockReset();
    uploadImageMock.mockReset();
    useCreateSplitMock.mockReturnValue({ mutate, isPending: false });
  });

  it('필수 입력 전 제출 버튼은 비활성', () => {
    renderCreate();
    expect(screen.getByRole('button', { name: '내 반띵 올리기' })).toBeDisabled();
  });

  it('가격/인원 입력 시 1인당 미리보기를 계산', async () => {
    renderCreate();
    await userEvent.type(screen.getByLabelText('전체 가격'), '20000');
    await userEvent.type(screen.getByLabelText('나눌 인원'), '2');
    expect(screen.getByText('₩10,000')).toBeInTheDocument();
  });

  it('점포 미선택 + 상세만 입력 시 제출 비활성 (실제 점포 선택 강제)', async () => {
    renderCreate();
    await fillProductFields();
    await userEvent.type(screen.getByLabelText('상세 위치'), '3층 KFC 앞');
    expect(screen.getByRole('button', { name: '내 반띵 올리기' })).toBeDisabled();
  });

  it('점포만 선택해도 제출 가능 (좌표는 선택 점포)', async () => {
    renderCreate();
    await fillProductFields();
    await userEvent.click(screen.getByRole('button', { name: '장소선택' }));
    const submit = screen.getByRole('button', { name: '내 반띵 올리기' });
    expect(submit).toBeEnabled();
    await userEvent.click(submit);
    expect(mutate.mock.calls[0][0]).toEqual({
      productName: '두쫀쿠',
      totalPrice: 20000,
      totalQty: 4,
      splitCount: 2,
      category: 'OTHER',
      latitude: 37.47,
      longitude: 127.04,
      address: '코스트코 양재점',
    });
  });

  it('점포 선택 + 상세 위치 → "점포명 · 상세" 형식 address, 좌표는 선택 점포', async () => {
    renderCreate();
    await fillProductFields();
    await userEvent.click(screen.getByRole('button', { name: '장소선택' }));
    await userEvent.type(screen.getByLabelText('상세 위치'), '아이파크몰 4층 스타벅스 정면');
    await userEvent.click(screen.getByRole('button', { name: '내 반띵 올리기' }));
    expect(mutate.mock.calls[0][0]).toEqual({
      productName: '두쫀쿠',
      totalPrice: 20000,
      totalQty: 4,
      splitCount: 2,
      category: 'OTHER',
      latitude: 37.47,
      longitude: 127.04,
      address: '코스트코 양재점 · 아이파크몰 4층 스타벅스 정면',
    });
  });

  it('지도 사용 불가 시 상세 위치만으로 제출 가능 (GPS 좌표 폴백)', async () => {
    renderCreate();
    await fillProductFields();
    await userEvent.click(screen.getByRole('button', { name: '지도불가' }));
    await userEvent.type(screen.getByLabelText('상세 위치'), '신용산역 2번 출구 골목 안쪽');
    const submit = screen.getByRole('button', { name: '내 반띵 올리기' });
    expect(submit).toBeEnabled();
    await userEvent.click(submit);
    expect(mutate.mock.calls[0][0]).toEqual({
      productName: '두쫀쿠',
      totalPrice: 20000,
      totalQty: 4,
      splitCount: 2,
      category: 'OTHER',
      latitude: 37.5665,
      longitude: 126.978,
      address: '신용산역 2번 출구 골목 안쪽',
    });
  });

  it('사진 추가 → 업로드 성공 시 imageUrl 이 payload 에 포함', async () => {
    pickImageMock.mockResolvedValue({
      blob: new Blob(['x'], { type: 'image/jpeg' }),
      contentType: 'image/jpeg',
    });
    uploadImageMock.mockResolvedValue('https://s3/img.jpg');
    renderCreate();

    await userEvent.click(screen.getByRole('button', { name: /사진 추가/ }));
    await waitFor(() => expect(uploadImageMock).toHaveBeenCalled());

    await fillProductFields();
    await userEvent.click(screen.getByRole('button', { name: '장소선택' }));
    await userEvent.click(screen.getByRole('button', { name: '내 반띵 올리기' }));
    expect(mutate.mock.calls[0][0]).toMatchObject({ imageUrl: 'https://s3/img.jpg' });
  });
});
