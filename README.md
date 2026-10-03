# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** ___ (preencher)

| Daniel Castro Sanches | RM563333 | 2CCPX |
| Maria Eduarda de Áquila Amaral | RM563783 | 2CCPX |
| Matheus Vilela Silveira | RM564989 | 2CCPX |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 12 / 12 |
| **Total de ajustes de Clean Code** | 6 / 6 |
| **Total de testes novos escritos** | 6 / 6 |
| **Suíte final (Run As → JUnit Test)** | 26 testes, 0 falhas |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | `GeradorProtocoloTest`: `assertSame` falhou (duas instâncias diferentes) e os protocolos vieram `1, 1, 1` em vez de `1, 2, 3` | `GeradorProtocolo.getInstancia()` (linha ~19) fazia `return new GeradorProtocolo()` e nunca guardava o resultado no campo `instancia`; além disso `proximo()` não era thread-safe, apesar do comentário prometer isso | `instancia = new GeradorProtocolo();` antes do retorno; `getInstancia()` e `proximo()` ficaram `synchronized` | Padrão Singleton (Aula 14), `static`, concorrência |
| bug02 | Banho pequeno custava R$ 100 e banho grande R$ 60 (contrato: 60 / 80 / 100). Só apareceu ao escrever o teste01 | `Banho.calcularPreco()` (linha ~28): valores de PEQUENO e GRANDE estavam trocados | PEQUENO → 60,0 e padrão (GRANDE) → 100,0 | Polimorfismo / regras de negócio no model (Aula 7 e 14) |
| bug03 | `new Tosa(...).getDuracaoMinutos()` devolvia 30 em vez de 60 (teste02) | `Tosa` (linha ~40) declarava `getDuracaoMinutos(String porte)`: isso é **sobrecarga**, não sobrescrita; a chamada polimórfica continuava usando o método da classe pai | Assinatura trocada para `@Override public int getDuracaoMinutos()` | Sobrescrita vs sobrecarga, `@Override` (Aula 7) |
| bug04 | `AtendimentoFactoryTest.devePreencherOsDadosDoPetNaConsulta`: `expected: <Mimi> but was: <null>` | Construtor de `ConsultaVeterinaria` (linha ~17) chamava `super()` vazio e descartava os parâmetros | `super(protocolo, petNome, petPorte, tutorNome, dataHora)` | Construtores e herança, `super` (Aula 6/7) |
| bug05 | `deveCriarTosaQuandoTipoForTosa` falhou: a factory devolvia um `Banho` | `AtendimentoFactory.criar` (linha ~17): `case "TOSA" -> new Banho(...)` (copiar e colar) | `case "TOSA" -> new Tosa(...)` | Padrão Factory (Aula 14) |
| bug06 | `deveMontarAtendimentoCompleto`: `expected: <Rex> but was: <null>` | `AtendimentoBuilder.comPet` (linha ~24): `petNome = petNome;` atribuía o parâmetro a ele mesmo (faltava `this`), o campo ficava `null` | `this.petNome = petNome;` | Escopo de variáveis, `this`, Builder (Aula 14) |
| bug07 | `deveRecusarMontagemSemNomeDoPet` / `SemPorte`: nenhuma exceção era lançada, objeto inválido nascia | `AtendimentoBuilder.construir` (linha ~41) não validava nada; o comentário dizia que isso era "por conta do controller" | `construir()` lança `IllegalArgumentException` se nome ou porte forem nulos/em branco | Builder, validação, "objeto só nasce válido", exceções (Aulas 11 e 14) |
| bug08 | `deveRecusarAgendamentoComHorarioJaOcupado`: agendamento duplicado passava (nenhuma `HorarioOcupadoException`) | `AgendaService.agendar` (linha ~23) comparava `getPetNome()` e `getDataHora()` com `==` (identidade de objeto). Funcionava "por sorte" com Strings literais internadas, mas falhava com `LocalDateTime` | `.equals()` nas duas comparações | `==` vs `equals()` (Aula 7) |
| bug09 | `deveLancarExcecaoQuandoAtendimentoNaoExiste`: nada era lançado, o método devolvia `null` | `AgendaService.buscarPorId` (linha ~40): `catch (Exception e) { return null; }` engolia a `AtendimentoNaoEncontradoException` | Removido o `try/catch`; o `orElseThrow` chega ao controller (que já devolve 404) | Tratamento de exceções, nunca engolir exceção (Aula 11) |
| bug10 | Cancelar um atendimento CONCLUIDO (ou já CANCELADO) funcionava. Só apareceu ao escrever o teste04 | `Atendimento.cancelar()` (linha ~63) não verificava o status | Só cancela se `AGENDADO`; senão lança `StatusInvalidoException` (o controller devolve 409) | Encapsulamento / regras de transição de status no model, exceções customizadas (Aula 11) |
| bug11 | Agendar com data no passado era aceito. Só apareceu ao escrever o teste06 | `AgendaService.agendar` não validava a data/hora | Valida no início (antes de consultar o repository): data nula ou antes de `now()` lança `IllegalArgumentException` (o controller devolve 400) | Validação de regra de negócio no service, exceções (Aula 11) |
| bug12 | (code review) Com a API de pé, todo `POST` falharia ao salvar | `Atendimento.id` (linha ~14) tinha `@Id` mas não `@GeneratedValue`: o JPA exige que o id seja atribuído manualmente, e o código nunca atribui | `@GeneratedValue(strategy = GenerationType.AUTO)` | JPA / mapeamento de entidades (Aulas 12 e 13) |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `AtendimentoFactory.criar(int p, String t, String n, String po, String tu, LocalDateTime d)` | Nomes significativos: parâmetros de uma letra e literais repetidos | Parâmetros renomeados (`protocolo`, `tipo`, `petNome`...) e `case` usando `Banho.TIPO`, `Tosa.TIPO`, `ConsultaVeterinaria.TIPO` |
| clean02 | `System.out.println` no `AgendaService.agendar` ("Recibo...") e no construtor do `GeradorProtocolo` | Sem efeitos colaterais/lixo de debug no código de produção; service só cuida da regra | Removidos os `println` |
| clean03 | `AtendimentoController`: método privado `calcularDescontoFidelidade` nunca usado + bloco de comentário de "futuro"; comentário do `POST` citava `?tutorNome=Ana` | Código morto e comentário enganoso | Removidos o método morto e o TODO; comentário corrigido |
| clean04 | `@Autowired` em campo no `AgendaService` e no `AtendimentoController` | Dependências explícitas e imutáveis (injeção por construtor); facilita teste | Campos `final` + construtor; `@InjectMocks` do teste continua funcionando (injeta via construtor) |
| clean05 | Strings mágicas `"AGENDADO"`, `"CONCLUIDO"`, `"CANCELADO"` espalhadas no model e no service | Evitar strings mágicas / duplicação (DRY) | Constantes `STATUS_AGENDADO/CONCLUIDO/CANCELADO` em `Atendimento` |
| clean06 | Strings `"PEQUENO"`/`"MEDIO"` em `Banho` e `Tosa` e número `30` no `getDuracaoMinutos` padrão | Evitar números e strings mágicos | Constantes `PORTE_PEQUENO`, `PORTE_MEDIO`, `DURACAO_PADRAO_MINUTOS` em `Atendimento` |

## Parte 3 — Testes novos (regras que estavam sem cobertura)

| # | Teste escrito (classe.método) | Regra coberta | Resultado ao escrever (vermelho/verde) |
|---|---|---|---|
| teste01 | `BanhoPrecoTest.deveCobrarPrecoCrescenteQuandoPorteAumenta` | Preço do banho: 60 / 80 / 100 por porte | **Vermelho**: revelou o bug02 |
| teste02 | `TosaDuracaoTest.deveDurar60MinutosQuandoUsadaComoAtendimento` | Tosa dura 60 min | **Vermelho**: revelou o bug03 |
| teste03 | `ConsultaPrecoTest.deveCustar150ReaisQuandoQualquerPorte` | Consulta com preço fixo de R$ 150, independente do porte | Verde de cara (regra já correta) |
| teste04 | `AtendimentoStatusTest.deveRecusarCancelamentoQuandoAtendimentoJaConcluido` | `cancelar()` recusa atendimento CONCLUIDO | **Vermelho**: revelou o bug10 |
| teste05 | `AtendimentoStatusTest.deveRecusarConclusaoQuandoAtendimentoCancelado` | `concluir()` recusa atendimento CANCELADO | Verde de cara (regra já correta) |
| teste06 | `AgendaServiceDataPassadoTest.deveRecusarAgendamentoQuandoDataHoraNoPassado` | Data/hora no passado → `IllegalArgumentException` e o banco nem é consultado | **Vermelho**: revelou o bug11 |

---

## Parte 4 — Perguntas de reflexão

> Rascunho baseado no código real do projeto. Revisem e reescrevam com as palavras do grupo antes de entregar.

### 1. A suíte como contrato (Aula 15)
Rodamos a suíte e cada mensagem de falha apontou um sintoma. O `expected: <Mimi> but was: <null>` do `AtendimentoFactoryTest` nos levou direto ao construtor da `ConsultaVeterinaria`, que chamava `super()` sem argumentos. O `assertSame` do `GeradorProtocoloTest` apontou o singleton que criava uma instância nova a cada chamada. Corrigimos um por vez e rodamos tudo de novo para ver se algo regrediu. O teste automatizado é melhor que o `curl` porque roda em segundos, sem banco e sem rede, repete sempre o mesmo cenário e protege contra regressões. Com o `curl` teríamos de subir a API, ter o Oracle no ar e conferir tudo na mão a cada mudança.

### 2. Mock e injeção de dependência (Aulas 13 a 15)
Em produção, o Spring cria o `AtendimentoRepository` real (Spring Data JPA) e injeta no `AgendaService` por construtor (antes era `@Autowired` em campo). No `AgendaServiceTest` quem faz esse papel é o Mockito: o `@Mock` cria um repository falso e o `@InjectMocks` o entrega ao service. Como o service só depende da interface `AtendimentoRepository`, ele não sabe se é o real ou o falso. Por isso o teste roda sem subir o Spring e sem Oracle: o mock responde só o que ensinamos com `when(...)`, e ainda podemos verificar chamadas, como `verify(repository, never()).save(any())`.

### 3. `==` vs `.equals()` (Aula 7)
No `AgendaService.agendar`, o conflito era checado com `a.getDataHora() == novo.getDataHora()`. O `==` compara se são o mesmo objeto na memória, não se têm o mesmo valor. No teste, a mesma data/hora vinha de outro objeto (`LocalDateTime.parse(...)`), então o `==` dava `false` e o agendamento duplicado passava. Com Strings literais como `"Rex"` o `==` "funciona por sorte", porque o Java reaproveita literais iguais do pool de Strings, mas isso quebra com Strings vindas de requisição, `new String` ou leitura do banco. A correção foi trocar por `.equals()` nas duas comparações.

### 4. Sobrescrita vs sobrecarga (Aula 7)
A `Tosa` declarava `getDuracaoMinutos(String porte)`. Isso é sobrecarga: um método novo, com outra assinatura, que convive com o `getDuracaoMinutos()` herdado de `Atendimento`. Quando o controller chama `atendimento.getDuracaoMinutos()`, o Java usa o método sem parâmetro e devolve 30 em vez de 60. Sobrescrita exige a mesma assinatura. Com `@Override`, o compilador reclamaria na hora que o método não sobrescreve nada da superclasse, e o bug nem chegaria a existir.

### 5. Singleton manual vs bean do Spring (Aula 14)
O `GeradorProtocolo` garante uma única instância (construtor privado, `getInstancia()` estático) e, com isso, uma numeração global sequencial. O bug era que `getInstancia()` fazia `return new GeradorProtocolo()` sem guardar o resultado em `instancia`, então cada chamada criava um gerador novo com contador zerado e todos os protocolos saíam como 1. Também deixamos os métodos `synchronized`, pois o comentário prometia thread-safety. O `AgendaService` não corre esse risco porque, com `@Service`, o container do Spring cria um único bean (escopo singleton por padrão) e o injeta onde for preciso. Não existe `getInstancia()` escrito à mão para esquecermos de guardar a instância.

### 6. Cobertura de testes: onde parar? (Aula 15)
Dos 6 testes novos, 4 ficaram vermelhos (bug02, bug03, bug10 e bug11) e 2 ficaram verdes de cara (preço fixo da consulta e concluir atendimento cancelado). Vale manter os verdes: eles documentam o contrato e protegem contra regressão, e rodam em milissegundos. Num projeto real com prazo, eu priorizaria o caminho feliz das regras de negócio principais (preço, pontos, agendamento) e os caminhos de erro que têm impacto financeiro ou de dados (conflito de horário, transições de status inválidas, data no passado). Perseguir 100% de cobertura tem retorno decrescente: getters e setters, por exemplo, dão pouco valor.

---