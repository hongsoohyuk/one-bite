export default function Home() {
  return (
    <main className="min-h-screen overflow-hidden">
      {/* Nav */}
      <nav className="fixed top-0 left-0 right-0 z-50 backdrop-blur-md bg-white/80 border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-2xl">🍊</span>
            <span className="text-xl font-bold text-onebite">한입만</span>
          </div>
          <a
            href="#download"
            className="px-5 py-2 bg-onebite text-white text-sm font-medium rounded-full hover:bg-onebite-dark transition-colors"
          >
            앱 다운로드
          </a>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative pt-32 pb-20 px-6 bg-gradient-to-br from-onebite-50 via-white to-onebite-light overflow-hidden">
        {/* Decorative blobs */}
        <div className="absolute top-20 right-[-100px] w-80 h-80 rounded-full bg-onebite/5 blur-3xl" />
        <div className="absolute bottom-0 left-[-80px] w-60 h-60 rounded-full bg-onebite/8 blur-2xl" />

        <div className="max-w-6xl mx-auto flex flex-col lg:flex-row items-center gap-12">
          <div className="flex-1 text-center lg:text-left">
            <div className="animate-fade-up">
              <span className="inline-block px-4 py-1.5 bg-onebite/10 text-onebite text-sm font-medium rounded-full mb-6">
                위치 기반 소셜 커머스
              </span>
            </div>

            <h1 className="animate-fade-up delay-100 text-5xl md:text-6xl lg:text-7xl font-black leading-tight tracking-tight opacity-0">
              아 이거
              <br />
              <span className="text-onebite">한입</span>만
              <br />
              먹고싶다
            </h1>

            <p className="animate-fade-up delay-200 mt-8 text-lg md:text-xl text-warm-gray leading-relaxed max-w-lg opacity-0">
              벌크/묶음 상품을 근처 사람과 나눠 구매하세요.
              <br />
              필요한 만큼만, 합리적인 가격으로.
            </p>

            <div className="animate-fade-up delay-300 mt-10 flex flex-col sm:flex-row gap-4 justify-center lg:justify-start opacity-0">
              <a
                href="#download"
                className="group px-8 py-4 bg-onebite text-white font-bold text-lg rounded-2xl hover:bg-onebite-dark transition-all hover:shadow-lg hover:shadow-onebite/25 hover:-translate-y-0.5"
              >
                시작하기
                <span className="inline-block ml-2 transition-transform group-hover:translate-x-1">
                  &rarr;
                </span>
              </a>
              <a
                href="#how"
                className="px-8 py-4 border-2 border-gray-200 text-warm-gray font-medium text-lg rounded-2xl hover:border-onebite hover:text-onebite transition-colors"
              >
                어떻게 되나요?
              </a>
            </div>
          </div>

          {/* Hero visual - phone mockup */}
          <div className="flex-1 flex justify-center animate-scale-in delay-200 opacity-0">
            <div className="relative">
              <div className="animate-float w-72 h-[500px] bg-warm-black rounded-[3rem] p-3 shadow-2xl shadow-warm-black/20">
                <div className="w-full h-full bg-white rounded-[2.3rem] overflow-hidden flex flex-col">
                  {/* Status bar */}
                  <div className="flex items-center justify-center pt-3 pb-2">
                    <div className="w-24 h-6 bg-warm-black rounded-full" />
                  </div>
                  {/* App header */}
                  <div className="px-5 py-3 border-b border-gray-100">
                    <div className="flex items-center gap-2">
                      <span className="text-lg">🍊</span>
                      <span className="text-base font-bold text-onebite">
                        한입만
                      </span>
                    </div>
                  </div>
                  {/* Content */}
                  <div className="flex-1 px-4 py-3 space-y-3 overflow-hidden">
                    {/* Split card 1 */}
                    <div className="bg-onebite-light rounded-xl p-3">
                      <div className="flex items-start gap-3">
                        <div className="w-12 h-12 bg-onebite/20 rounded-lg flex items-center justify-center text-xl shrink-0">
                          🧁
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-bold truncate">
                            두쫀쿠 4개입
                          </p>
                          <p className="text-xs text-warm-gray mt-0.5">
                            서울 강남구 · 300m
                          </p>
                          <p className="text-sm font-bold text-onebite mt-1">
                            10,000원 / 1인
                          </p>
                        </div>
                      </div>
                    </div>
                    {/* Split card 2 */}
                    <div className="bg-gray-50 rounded-xl p-3">
                      <div className="flex items-start gap-3">
                        <div className="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center text-xl shrink-0">
                          🥚
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-bold truncate">
                            유기농 달걀 30구
                          </p>
                          <p className="text-xs text-warm-gray mt-0.5">
                            서울 서초구 · 1.2km
                          </p>
                          <p className="text-sm font-bold text-onebite mt-1">
                            6,500원 / 1인
                          </p>
                        </div>
                      </div>
                    </div>
                    {/* Split card 3 */}
                    <div className="bg-gray-50 rounded-xl p-3">
                      <div className="flex items-start gap-3">
                        <div className="w-12 h-12 bg-yellow-50 rounded-lg flex items-center justify-center text-xl shrink-0">
                          🧴
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-bold truncate">
                            대용량 세제 5L
                          </p>
                          <p className="text-xs text-warm-gray mt-0.5">
                            서울 강남구 · 800m
                          </p>
                          <p className="text-sm font-bold text-onebite mt-1">
                            4,000원 / 1인
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                  {/* Bottom nav */}
                  <div className="px-6 py-3 border-t border-gray-100 flex justify-around">
                    <div className="flex flex-col items-center gap-0.5">
                      <div className="w-5 h-5 bg-onebite rounded-sm" />
                      <span className="text-[10px] text-onebite font-medium">
                        홈
                      </span>
                    </div>
                    <div className="flex flex-col items-center gap-0.5">
                      <div className="w-5 h-5 bg-gray-300 rounded-sm" />
                      <span className="text-[10px] text-gray-400">지도</span>
                    </div>
                    <div className="flex flex-col items-center gap-0.5">
                      <div className="w-5 h-5 bg-gray-300 rounded-sm" />
                      <span className="text-[10px] text-gray-400">프로필</span>
                    </div>
                  </div>
                </div>
              </div>
              {/* Floating badges */}
              <div className="absolute -left-12 top-24 bg-white rounded-2xl px-4 py-2.5 shadow-xl shadow-black/5 animate-float delay-300">
                <span className="text-sm font-medium">📍 300m 거리</span>
              </div>
              <div className="absolute -right-10 bottom-32 bg-white rounded-2xl px-4 py-2.5 shadow-xl shadow-black/5 animate-float delay-500">
                <span className="text-sm font-medium">
                  💰 반값에 나눠갖기
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how" className="py-24 px-6 bg-white">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-black">
              <span className="text-onebite">4단계</span>로 끝
            </h2>
            <p className="mt-4 text-warm-gray text-lg">
              매장에서 발견, 앱에서 등록, 근처에서 매칭
            </p>
          </div>

          <div className="grid md:grid-cols-4 gap-6">
            {[
              {
                step: "01",
                emoji: "👀",
                title: "발견",
                desc: "매장에서 두쫀쿠 4개입(2만원)을 발견. 근데 2개만 원해...",
              },
              {
                step: "02",
                emoji: "📱",
                title: "등록",
                desc: "앱에서 상품 등록. 위치, 사진, 가격, 나눌 수량을 입력",
              },
              {
                step: "03",
                emoji: "🔔",
                title: "매칭",
                desc: "근처 유저에게 알림! \"300m 거리에서 두쫀쿠 나눠요\"",
              },
              {
                step: "04",
                emoji: "🤝",
                title: "완료",
                desc: "만나서 상품과 금액을 교환. 둘 다 반값에 GET!",
              },
            ].map((item, i) => (
              <div
                key={item.step}
                className="group relative bg-warm-light hover:bg-onebite-light rounded-3xl p-8 transition-all duration-300 hover:-translate-y-1 hover:shadow-lg"
              >
                <span className="text-6xl font-black text-gray-100 group-hover:text-onebite/10 transition-colors absolute top-4 right-6">
                  {item.step}
                </span>
                <div className="relative">
                  <span className="text-4xl block mb-4">{item.emoji}</span>
                  <h3 className="text-xl font-bold mb-2">{item.title}</h3>
                  <p className="text-warm-gray text-sm leading-relaxed">
                    {item.desc}
                  </p>
                </div>
                {i < 3 && (
                  <div className="hidden md:block absolute top-1/2 -right-4 text-2xl text-gray-300">
                    &rarr;
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-24 px-6 bg-warm-light">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-black">
              왜 <span className="text-onebite">한입만</span>인가요?
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="bg-white rounded-3xl p-10 hover:shadow-xl transition-all duration-300 hover:-translate-y-1 group">
              <div className="w-16 h-16 bg-onebite/10 rounded-2xl flex items-center justify-center text-3xl mb-6 group-hover:bg-onebite/20 transition-colors">
                📍
              </div>
              <h3 className="text-2xl font-bold mb-3">위치 기반 매칭</h3>
              <p className="text-warm-gray leading-relaxed">
                GPS로 내 주변의 나눠사기를 실시간으로 찾아요. 걸어갈 수 있는
                거리에서 바로 거래할 수 있어요.
              </p>
              <div className="mt-6 flex items-center gap-3">
                <div className="flex -space-x-2">
                  <div className="w-8 h-8 bg-onebite rounded-full border-2 border-white flex items-center justify-center text-xs text-white font-bold">
                    A
                  </div>
                  <div className="w-8 h-8 bg-amber-400 rounded-full border-2 border-white flex items-center justify-center text-xs text-white font-bold">
                    B
                  </div>
                </div>
                <span className="text-sm text-warm-gray">
                  반경 3km 이내 매칭
                </span>
              </div>
            </div>

            {/* Feature 2 */}
            <div className="bg-white rounded-3xl p-10 hover:shadow-xl transition-all duration-300 hover:-translate-y-1 group">
              <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center text-3xl mb-6 group-hover:bg-blue-100 transition-colors">
                ⚡
              </div>
              <h3 className="text-2xl font-bold mb-3">실시간 알림</h3>
              <p className="text-warm-gray leading-relaxed">
                근처에서 누군가 나눠사기를 등록하면 바로 푸시 알림이 와요.
                놓치지 않고 참여할 수 있어요.
              </p>
              <div className="mt-6 bg-gray-50 rounded-xl p-3">
                <div className="flex items-center gap-2">
                  <span className="text-lg">🔔</span>
                  <div>
                    <p className="text-xs font-medium">지금 · 300m</p>
                    <p className="text-xs text-warm-gray">
                      &ldquo;두쫀쿠 반씩 나눠요&rdquo;
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Feature 3 */}
            <div className="bg-white rounded-3xl p-10 hover:shadow-xl transition-all duration-300 hover:-translate-y-1 group">
              <div className="w-16 h-16 bg-green-50 rounded-2xl flex items-center justify-center text-3xl mb-6 group-hover:bg-green-100 transition-colors">
                🛡️
              </div>
              <h3 className="text-2xl font-bold mb-3">안전 거래</h3>
              <p className="text-warm-gray leading-relaxed">
                프로필 인증과 거래 이력으로 신뢰할 수 있는 거래 환경을
                제공해요. 안심하고 나눠사세요.
              </p>
              <div className="mt-6 flex gap-2">
                {["프로필 인증", "거래 이력", "신고 시스템"].map((tag) => (
                  <span
                    key={tag}
                    className="px-3 py-1 bg-green-50 text-green-700 text-xs font-medium rounded-full"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Example savings */}
      <section className="py-24 px-6 bg-white">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-black mb-4">
            이만큼 <span className="text-onebite">절약</span>돼요
          </h2>
          <p className="text-warm-gray text-lg mb-12">
            실제 한입만 이용 시나리오
          </p>

          <div className="grid sm:grid-cols-3 gap-6">
            {[
              {
                item: "코스트코 크루아상 12개",
                full: "16,900",
                split: "8,450",
                people: 2,
              },
              {
                item: "대용량 세탁 세제 5L",
                full: "24,000",
                split: "8,000",
                people: 3,
              },
              {
                item: "견과류 선물세트",
                full: "35,000",
                split: "17,500",
                people: 2,
              },
            ].map((ex) => (
              <div
                key={ex.item}
                className="bg-warm-light rounded-3xl p-8 text-left"
              >
                <p className="font-bold text-lg mb-4">{ex.item}</p>
                <div className="space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-warm-gray">원래 가격</span>
                    <span className="text-sm line-through text-gray-400">
                      {ex.full}원
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium">
                      {ex.people}명이서 나누면
                    </span>
                    <span className="text-xl font-black text-onebite">
                      {ex.split}원
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA / Download */}
      <section
        id="download"
        className="py-24 px-6 bg-gradient-to-br from-onebite to-onebite-dark text-white"
      >
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-black mb-4">
            지금 바로 시작하세요
          </h2>
          <p className="text-white/80 text-lg mb-12">
            필요한 만큼만 사는 똑똑한 소비, 한입만과 함께
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            {/* App Store */}
            <button
              disabled
              className="group flex items-center gap-3 px-8 py-4 bg-white/15 backdrop-blur rounded-2xl border border-white/20 hover:bg-white/25 transition-colors cursor-not-allowed"
            >
              <svg
                className="w-8 h-8"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" />
              </svg>
              <div className="text-left">
                <p className="text-xs text-white/60">Coming Soon</p>
                <p className="text-base font-bold">App Store</p>
              </div>
            </button>

            {/* Google Play */}
            <button
              disabled
              className="group flex items-center gap-3 px-8 py-4 bg-white/15 backdrop-blur rounded-2xl border border-white/20 hover:bg-white/25 transition-colors cursor-not-allowed"
            >
              <svg
                className="w-8 h-8"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M3 20.5v-17c0-.59.34-1.11.84-1.35L13.69 12l-9.85 9.85c-.5-.24-.84-.76-.84-1.35m13.81-5.38L6.05 21.34l8.49-8.49 2.27 2.27m3.35-4.31c.34.27.56.69.56 1.19s-.22.92-.56 1.19l-1.97 1.13-2.5-2.5 2.5-2.5 1.97 1.49M6.05 2.66l10.76 6.22-2.27 2.27-8.49-8.49z" />
              </svg>
              <div className="text-left">
                <p className="text-xs text-white/60">Coming Soon</p>
                <p className="text-base font-bold">Google Play</p>
              </div>
            </button>
          </div>

          <p className="mt-8 text-sm text-white/50">
            2026년 상반기 출시 예정
          </p>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 px-6 bg-warm-black text-white">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row justify-between items-center gap-6">
            <div className="flex items-center gap-2">
              <span className="text-xl">🍊</span>
              <span className="text-lg font-bold">한입만</span>
              <span className="text-sm text-gray-500 ml-2">OneBite</span>
            </div>

            <div className="flex gap-6 text-sm text-gray-400">
              <a href="/privacy" className="hover:text-white transition-colors">
                개인정보처리방침
              </a>
              <a href="/terms" className="hover:text-white transition-colors">
                이용약관
              </a>
              <a
                href="mailto:hello@onebite.app"
                className="hover:text-white transition-colors"
              >
                문의하기
              </a>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-gray-800 text-center text-xs text-gray-500">
            <p>&copy; 2026 OneBite. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </main>
  );
}
