# Backend — FD SmartCheck (Spring Boot)

## Package structure
```
org.fdsmartcheck
├── config/          → SecurityConfig, CorsConfig, AuditConfig
├── controller/      → REST controllers (um por agregado)
├── dto/
│   ├── request/     → DTOs de entrada (@Valid, @NotNull, etc.)
│   └── response/    → DTOs de saída (nunca expor entidade diretamente)
├── exception/       → GlobalExceptionHandler + exceções customizadas
├── model/           → Entidades JPA
│   └── enums/       → Role, EventStatus
├── repository/      → Interfaces JpaRepository
├── security/        → JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
├── service/         → Lógica de negócio (um por agregado)
└── util/            → Utilitários (GeoUtils)
```

## Convenções obrigatórias

### Entidades JPA
- Lombok obrigatório: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`
- Auditoria via `@EntityListeners(AuditingEntityListener.class)` com `@CreatedDate` / `@LastModifiedDate`
- ID: `String` com `@GeneratedValue(strategy = GenerationType.UUID)`
- FetchType padrão para `@ManyToOne`: `LAZY` — nunca `EAGER`
- Relacionamentos N-N usam entidade intermediária com lógica (Subscription, Check) — não `@ManyToMany`

```java
// Padrão de entidade
@Entity
@Table(name = "table_name")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ...campos...

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Controllers
- Um controller por agregado, mapeado em `/api/{recurso}`
- Retorno sempre `ResponseEntity<T>`
- Criação retorna `201 Created` com `Location` header via `ServletUriComponentsBuilder`
- Deleção retorna `204 No Content`
- `@PreAuthorize("hasRole('ADMIN')")` para endpoints restritos
- Validação de entrada via `@Valid` no `@RequestBody`
- Controller não contém lógica — só delega para service

```java
@RestController
@RequestMapping("/api/recurso")
@RequiredArgsConstructor
public class RecursoController {
    private final RecursoService recursoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecursoResponse> create(@Valid @RequestBody RecursoRequest request) {
        RecursoResponse created = recursoService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }
}
```

### Services
- Toda lógica de negócio fica no service
- Lança exceções customizadas: `ResourceNotFoundException` (404), `BadRequestException` (400), `UnauthorizedException` (401)
- Nunca retorna entidade JPA — converte para DTO de response antes de retornar
- Usa `@Transactional` onde necessário (operações de escrita)
- Acessa usuário autenticado via `SecurityContextHolder`

```java
// Pegar usuário autenticado (o "name" do principal é sempre o id do usuário,
// independente de o login ter sido feito por email ou RA)
String userId = SecurityContextHolder.getContext().getAuthentication().getName();
User user = userRepository.findById(userId)
    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
```

### DTOs
- **Request**: campos com `@NotNull`, `@NotBlank`, `@Valid` conforme necessário
- **Response**: campos públicos, construtor/builder para conversão da entidade
- Nunca reutilizar o mesmo DTO para request e response

### Tratamento de erros
Centralizado em `GlobalExceptionHandler`. Para adicionar novo tipo de erro, criar a exceção e registrar o handler lá. Não use try-catch nos controllers para erros de negócio.

### Segurança
- Rotas públicas configuradas em `SecurityConfig` — qualquer nova rota pública deve ser adicionada lá
- JWT validado em `JwtAuthenticationFilter` → extrai o id do usuário do token (subject) → carrega `UserDetails`
- Login aceita email ou RA (`UserDetailsServiceImpl.loadUserByUsername` resolve por id, depois email, depois RA); o subject do JWT é sempre o `user.getId()`, nunca o email, para que a identidade não dependa de qual credencial foi usada no login
- Geolocalização validada via `GeoSecurityService.validateLocation()` usando raio em metros

## Stack de testes
- JUnit 5 (`@ExtendWith(MockitoExtension.class)`)
- Mockito para mocks de repository e dependências
- `@SpringBootTest` para testes de integração
- Teste de controller: `@WebMvcTest` + `MockMvc`

```java
// Padrão unitário de service
@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock private EventRepository eventRepository;
    @InjectMocks private EventService eventService;

    @Test
    void shouldThrowWhenEventNotFound() {
        when(eventRepository.findById("id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
            () -> eventService.getEventById("id"));
    }
}
```

## Como buildar e rodar
```bash
./mvnw clean compile          # compilar
./mvnw test                   # rodar testes
./mvnw spring-boot:run        # rodar local (porta 8080)
./mvnw clean package -DskipTests  # gerar jar
```

## Variáveis de ambiente necessárias
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MS
```

## O que NÃO fazer
- Nunca colocar lógica de negócio no controller
- Nunca usar `FetchType.EAGER` — causa N+1
- Nunca expor a entidade JPA diretamente como response
- Nunca fazer join fetch sem necessidade — use projeções ou DTOs
- Não criar novos `@ManyToMany` — usar entidade intermediária
- Não ignorar `@Transactional` em operações que modificam múltiplas entidades
