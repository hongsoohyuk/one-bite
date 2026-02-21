# Kotlin 학습 로드맵

> 대상: JS/TS 경험자 | 목표: Spring Boot 서버 + KMP 모바일 앱 개발

## Phase 1: Kotlin 기초 문법 (3~5일)

JS/TS를 아니까 빠르게 갈 수 있다. "다른 점"에 집중.

### Day 1: 변수, 타입, 함수

```kotlin
// JS: const, let, var
// Kotlin: val (불변), var (가변) — const는 컴파일타임 상수에만 사용
val name: String = "한입"
var count = 4  // 타입 추론 가능

// JS: function add(a, b) { return a + b }
// Kotlin:
fun add(a: Int, b: Int): Int {
    return a + b
}

// 단일 표현식 함수 (한 줄이면 이렇게)
fun add(a: Int, b: Int) = a + b

// JS: (a, b) => a + b
// Kotlin:
val add = { a: Int, b: Int -> a + b }

// Nullable — TS의 string | null과 비슷하지만 더 엄격
var nickname: String? = null  // ?가 있어야 null 가능
nickname?.length              // optional chaining (JS의 ?. 와 동일)
nickname ?: "익명"            // Elvis 연산자 (JS의 ?? 와 동일)
nickname!!                    // 강제 non-null (쓰지 마라, 터진다)
```

**연습**: 상품 가격을 인원수로 나누는 함수 만들기 (null 처리 포함)

### Day 2: 클래스, data class, enum

```kotlin
// JS: class Product { constructor(name, price) { ... } }
// Kotlin:
class Product(val name: String, val price: Int)

// data class — JS 객체처럼 쓸 수 있는 클래스
// equals, hashCode, toString, copy 자동 생성
data class Product(
    val name: String,
    val price: Int,
    val quantity: Int,
    val imageUrl: String? = null  // 기본값 (JS의 default parameter)
)

val product = Product("두쫀쿠", 20000, 4)
val half = product.copy(quantity = 2, price = 10000)  // 불변 업데이트 (spread처럼)

// enum
enum class SplitStatus {
    WAITING,    // 나눌 사람 대기중
    MATCHED,    // 매칭됨
    COMPLETED,  // 거래 완료
    CANCELLED   // 취소
}

// sealed class — TS의 discriminated union과 비슷
sealed class Result {
    data class Success(val data: Product) : Result()
    data class Error(val message: String) : Result()
    object Loading : Result()
}

// 패턴 매칭 (TS의 switch + type narrowing)
when (result) {
    is Result.Success -> println(result.data.name)
    is Result.Error -> println(result.message)
    Result.Loading -> println("로딩중...")
}
```

**연습**: One Bite 앱의 `SplitRequest` data class 설계하기

### Day 3: 컬렉션, 람다, 고차함수

```kotlin
// JS 배열 메서드와 거의 1:1 대응
val products = listOf("두쫀쿠", "캠벨스프", "코카콜라")  // 불변 리스트
val mutableList = mutableListOf<String>()                // 가변 리스트

// JS: products.filter(p => p.includes("쿠"))
products.filter { it.contains("쿠") }    // it = 단일 파라미터 축약

// JS: products.map(p => p.length)
products.map { it.length }

// JS: products.find(p => p === "두쫀쿠")
products.find { it == "두쫀쿠" }         // === 없음, == 이 구조적 비교

// JS: products.reduce((acc, p) => acc + p.length, 0)
products.sumOf { it.length }

// Map (JS의 Object / Map)
val priceMap = mapOf(
    "두쫀쿠" to 20000,
    "캠벨스프" to 15000
)
priceMap["두쫀쿠"]  // 20000

// 구조 분해 (JS destructuring)
val (name, price) = Pair("두쫀쿠", 20000)
data class Point(val x: Int, val y: Int)
val (x, y) = Point(37, 127)  // data class도 가능
```

**연습**: 상품 리스트에서 가격 필터링, 정렬, 변환 체이닝 해보기

### Day 4: null 안전성, 스코프 함수, 확장 함수

```kotlin
// 스코프 함수 — Kotlin만의 독특한 패턴
// let: null 체크 + 변환 (JS의 optional chaining + map 느낌)
val price: Int? = null
price?.let { println("가격: ${it}원") }  // null이면 실행 안됨

// apply: 객체 초기화 (JS에는 없는 패턴, Builder 대용)
val request = SplitRequest().apply {
    productName = "두쫀쿠"
    price = 20000
    splitCount = 2
}

// also: 부수효과 (디버깅에 유용)
products
    .filter { it.price > 10000 }
    .also { println("필터 결과: $it") }  // 중간 확인
    .map { it.name }

// 확장 함수 — 기존 클래스에 메서드 추가 (JS prototype 수정과 비슷하지만 안전)
fun Int.toFormattedPrice(): String = "${String.format("%,d", this)}원"
20000.toFormattedPrice()  // "20,000원"

fun Product.splitPrice(count: Int): Int = this.price / count
```

**연습**: `String.toProduct()` 확장 함수 만들어보기

### Day 5: 코루틴 기초 (비동기 처리)

```kotlin
// JS: async/await
// Kotlin: suspend + coroutine

// JS:  async function fetchProduct(id) { ... }
// Kotlin:
suspend fun fetchProduct(id: String): Product {
    // 네트워크 호출 등 비동기 작업
    return api.getProduct(id)  // 자동으로 대기
}

// JS:  await fetchProduct("123")
// Kotlin:
// suspend 함수 안에서 그냥 호출하면 됨 (await 키워드 불필요)
suspend fun main() {
    val product = fetchProduct("123")  // 알아서 대기
    println(product.name)
}

// 병렬 실행 (JS: Promise.all)
coroutineScope {
    val product = async { fetchProduct("123") }
    val user = async { fetchUser("456") }
    // 둘 다 완료될 때까지 대기
    println("${product.await()} by ${user.await()}")
}

// 에러 처리 (JS: try/catch 동일)
try {
    val product = fetchProduct("123")
} catch (e: Exception) {
    println("에러: ${e.message}")
}
```

**연습**: 가짜 delay로 상품 조회 시뮬레이션 해보기

---

## Phase 2: Spring Boot 기초 (1주)

### Day 6~7: 프로젝트 셋업, REST API

```kotlin
// Controller — Next.js의 API Route와 비슷한 역할
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService  // 의존성 주입 (자동)
) {
    // GET /api/products
    @GetMapping
    fun getProducts(): List<Product> = productService.findAll()

    // GET /api/products/{id}
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): Product =
        productService.findById(id)

    // POST /api/products
    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): Product =
        productService.create(request)
}
```

JS/TS 대응 관계:
| Next.js | Spring Boot |
|---------|-------------|
| `app/api/products/route.ts` | `@RestController` |
| `export async function GET()` | `@GetMapping` |
| `export async function POST()` | `@PostMapping` |
| `req.body` | `@RequestBody` |
| `params.id` | `@PathVariable` |
| middleware | `@Component` + Filter/Interceptor |

### Day 8~9: 데이터베이스 (JPA)

```kotlin
// Entity — DB 테이블과 매핑 (Prisma 모델과 비슷)
@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val price: Int,
    val quantity: Int,
    val latitude: Double,
    val longitude: Double,
    val status: SplitStatus = SplitStatus.WAITING,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Repository — Prisma client처럼 DB 쿼리 담당
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByStatus(status: SplitStatus): List<Product>
    // 메서드 이름만으로 쿼리 자동 생성!
}
```

### Day 10~12: 인증, 에러 처리, 테스트
- Spring Security + 카카오 OAuth2
- GlobalExceptionHandler
- JUnit5 + MockK 테스트 기본

---

## Phase 3: KMP 모바일 기초 (1~2주)

### Compose UI 기초 (Day 13~15)

```kotlin
// React 컴포넌트와 거의 동일한 멘탈 모델
// React:
// function ProductCard({ product }) {
//   const [expanded, setExpanded] = useState(false)
//   return <div onClick={() => setExpanded(!expanded)}>...</div>
// }

// Compose:
@Composable
fun ProductCard(product: Product) {
    var expanded by remember { mutableStateOf(false) }  // useState

    Column(
        modifier = Modifier
            .clickable { expanded = !expanded }  // onClick
            .padding(16.dp)
    ) {
        Text(product.name, style = MaterialTheme.typography.titleMedium)
        Text(product.price.toFormattedPrice())
        if (expanded) {  // 조건부 렌더링 (JSX의 {expanded && ...})
            Text("수량: ${product.quantity}개")
        }
    }
}
```

React → Compose 대응:
| React | Compose |
|-------|---------|
| `useState` | `remember { mutableStateOf() }` |
| `useEffect` | `LaunchedEffect` |
| `useMemo` | `remember { derivedStateOf() }` |
| `<div>` | `Column`, `Row`, `Box` |
| `className` / `style` | `Modifier` |
| props | 함수 파라미터 |
| children | `content: @Composable () -> Unit` |
| Context | `CompositionLocal` |

### 네비게이션, 상태관리 (Day 16~17)

### 네이티브 연동 - 카메라, 위치, 푸시 (Day 18~19)

---

## 학습 리소스

### 필수 (무료)
1. **Kotlin Playground** — https://play.kotlinlang.org (브라우저에서 바로 실습)
2. **Kotlin Koans** — https://kotlinlang.org/docs/koans.html (공식 연습문제)
3. **Spring Boot 공식 가이드** — https://spring.io/guides
4. **Compose Multiplatform 튜토리얼** — JetBrains 공식

### 추천 (유료)
1. Kotlin in Action (책) — 언어 깊게 이해할 때
2. JetBrains Academy — Kotlin 트랙

### 학습 팁
- **Kotlin Playground에서 Day 1~5 전부 실습 가능** (설치 없이)
- Day 6부터 IntelliJ IDEA 설치 필요
- Day 13부터 Android Studio 설치 필요
- 모르겠으면 "이거 JS로 하면 이런건데 Kotlin으로 어떻게 해?" 라고 물어보면 됨

---

## 체크포인트 과제

각 Phase 끝날 때 직접 만들어볼 것:

### Phase 1 완료 과제
상품 분할 계산기 CLI 앱 만들기:
- 상품명, 가격, 수량 입력 받기
- 나눌 인원수 입력
- 1인당 가격 계산 + 포맷팅 출력
- data class, 확장 함수, null 처리 활용

### Phase 2 완료 과제
One Bite 상품 CRUD REST API 만들기:
- 상품 등록/조회/수정/삭제
- 위치 좌표 저장
- 상태(WAITING/MATCHED/COMPLETED) 관리
- 기본 에러 처리

### Phase 3 완료 과제
One Bite 모바일 MVP 화면 만들기:
- 상품 리스트 화면 (서버 API 연동)
- 상품 등록 화면 (폼)
- 지도에 상품 핀 표시
