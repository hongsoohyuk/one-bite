import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "한입만 - 벌크 상품 나눠사기",
  description:
    "아 이거 한입만 먹고싶다 - 벌크/묶음 상품을 근처 사람과 나눠 구매하는 위치 기반 소셜 커머스",
  openGraph: {
    title: "한입만 - 벌크 상품 나눠사기",
    description: "근처 사람과 벌크 상품을 나눠 구매하세요",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <head>
        <link
          href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&family=Playfair+Display:wght@400;700;900&display=swap"
          rel="stylesheet"
        />
      </head>
      <body
        className="font-display antialiased text-warm-black"
        style={{ fontFamily: "'Noto Sans KR', sans-serif" }}
      >
        {children}
      </body>
    </html>
  );
}
