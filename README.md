# Xcore — Robôs de combate para Robocode

Projeto acadêmico desenvolvido em Java para a plataforma [Robocode](https://robocode.sourceforge.io/). O repositório contém três robôs de combate que herdam de `AdvancedRobot` e pertencem ao pacote Java `xcore`:

- **Xcore** — desenvolvido por **Davi de Jesus**.

- **Ultron** — desenvolvido por **Matheus**.

- **Megatron** — desenvolvido por **Matheus**.

A documentação concentra-se no Xcore, que combina movimentação aleatória, fuga de colisões e impactos, varredura contínua do radar, alinhamento independente de canhão e radar e potência de tiro variável conforme a distância do adversário.

## Estrutura do repositório

```
.
├── Xcore.java       # Robô desenvolvido por Davi de Jesus
├── Ultron.java      # Robô desenvolvido por Matheus
├── Megatron.java    # Robô desenvolvido por Matheus
├── docs/            # Materiais da atividade acadêmica em PDF
├── LICENSE          # MIT License
└── README.md
```

As três classes declaram `package xcore` e estendem `robocode.AdvancedRobot`. A compilação e a execução dependem da instalação do Robocode e da sua API Java.

## Arquitetura comum

Cada robô é uma classe independente orientada a eventos. O método `run()` configura cores, ajustes de rotação e o comportamento contínuo. Os métodos `onScannedRobot`, `onHitByBullet`, `onHitWall` e outros callbacks são chamados pelo motor do Robocode em resposta aos eventos da batalha.

O Xcore usa `setAdjustGunForRobotTurn(true)` e `setAdjustRadarForGunTurn(true)`, mantendo a rotação do canhão independente da rotação do corpo e a rotação do radar independente da rotação do canhão. Ultron também separa o radar do corpo com `setAdjustRadarForRobotTurn(true)`. Megatron separa o canhão e o radar do corpo, mas não chama explicitamente o terceiro ajuste.

## Xcore

### Configuração inicial

A classe `Xcore` estende `AdvancedRobot`, cria um `Random` no construtor e utiliza:

- corpo azul (`Color.BLUE`);

- canhão em tom azul-claro;

- radar em tom ciano;

- constante `GIRO = 360` para giros completos.

No início de `run()`, o robô configura as cores, desacopla as rotações do canhão e do radar e solicita uma varredura inicial de 360 graus. Depois entra em um loop contínuo que chama `execute()` para enviar ao motor os comandos de movimento, rotação e radar.

### Movimentação principal

Enquanto `isStop` for `false`, o Xcore:

1. solicita um avanço com distância aleatória entre **80 e 149** unidades, retornada por `randomRoad()`;

1. chama `rotationEscape()`, que pode adicionar uma rotação à direita e/ou à esquerda.

`rotationEscape()` sorteia uma rotação à direita entre 0 e 69 graus. Somente valores entre 26 e 49 geram um `setTurnRight`. Quando essa condição ocorre, o método sorteia também uma distância entre 80 e 149 para a esquerda; se essa distância estiver entre 86 e 124, ela é descartada, caso contrário é emitido um `setTurnLeft`. Assim, a trajetória não é uma sequência fixa: ela combina avanços aleatórios com mudanças ocasionais de direção.

A cada iteração, se `getRadarTurnRemaining()` for zero, o radar recebe uma nova solicitação de giro de 360 graus. Isso mantém a busca ativa quando a ordem anterior termina.

### Mira e radar

Quando o radar encontra um robô, `onScannedRobot(ScannedRobotEvent e)` calcula o ângulo absoluto do adversário somando o heading do corpo ao bearing informado pelo evento. Em seguida:

- normaliza a diferença entre o ângulo absoluto e o heading do canhão com `normalRelativeAngleDegrees`;

- gira o canhão pelo menor ângulo relativo até o alvo;

- calcula a diferença entre o alvo e o heading do radar e a multiplica por 2;

- gira o radar para manter a varredura sobre a região do adversário.

O código não implementa previsão da velocidade ou da direção do inimigo. A mira é baseada na posição angular observada no evento de varredura.

### Potência de tiro

O Xcore solicita potência de tiro de acordo com a distância detectada:

| Distância do inimigo   | Potência solicitada |
| ---------------------- | ------------------- |
| menor que 100          | `3.0`               |
| de 100 a menor que 200 | `2.5`               |
| de 200 a menor que 300 | `1.5`               |
| 300 ou mais            | `1.0`               |

Não há uma verificação explícita de `getGunHeat()` no Xcore antes de chamar `setFire`. A decisão de potência ocorre sempre que `onScannedRobot` é acionado.

Quando o adversário está a menos de 50 unidades, o robô solicita um novo avanço aleatório para se afastar ou reposicionar em curta distância.

### Resposta a impactos e colisões

Os eventos `onHitByBullet`, `onHitWall` e `onHitRobot` encaminham o bearing para o método privado `onHit(double bearing)`.

Esse método:

- redefine `isStop` para `false`;

- usa 90 unidades como distância de fuga;

- usa 45 graus como rotação de escape;

- se o bearing estiver entre -90 e 90 graus, gira 45 graus à direita e recua 90 unidades;

- caso contrário, gira 45 graus à esquerda e avança 90 unidades.

A mesma lógica é reutilizada para tiro recebido, parede e colisão com outro robô. Isso evita manter três implementações distintas para a reação de escape.

### Estado `isStop`

Quando o Xcore acerta uma bala, `onBulletHit` define `isStop = true`. Enquanto esse estado permanece verdadeiro, o loop principal deixa de emitir os comandos regulares de avanço e rotação. Quando uma bala falha, `onBulletMissed` define `isStop = false`, permitindo que a movimentação principal volte a ser emitida.

Esse comportamento é específico do código atual: o robô pausa a emissão da movimentação normal depois de um acerto e retoma-a depois de um disparo perdido. Os eventos de impacto e colisão também redefinem o estado para permitir a fuga.

### Vitória

`onWin(WinEvent event)` solicita uma rotação completa de 360 graus à direita.

## Estratégias dos outros robôs

### Ultron — desenvolvido por Matheus

Ultron também é um `AdvancedRobot` e usa uma estratégia mais direta:

- cores: corpo cinza, canhão vermelho e radar cinza;

- corpo, canhão e radar configurados para girarem de forma independente;

- radar girando continuamente com `setTurnRadarRight(Double.POSITIVE_INFINITY)`;

- movimento contínuo de 150 unidades e rotação de 45 graus, controlados por `direcao`;

- inversão de `direcao` ao ser atingido por bala ou ao bater na parede;

- mira baseada no ângulo relativo entre o heading do corpo, o bearing do inimigo e o heading do canhão;

- tiro somente quando `getGunHeat() == 0`;

- potência 3 para menos de 150 unidades, 2 para menos de 400 e 1 nas demais distâncias.

Ao receber uma bala, Ultron inverte a direção, avança 200 unidades e gira 90 graus. Ao atingir uma parede, inverte a direção, recua 100 unidades e gira 90 graus.

### Megatron — desenvolvido por Matheus

Megatron mantém um `HashMap<String, Double>` com o nome dos inimigos e a última distância observada de cada um. A variável `alvo` guarda o nome do alvo escolhido. Em cada varredura, o robô atualiza a distância e seleciona o inimigo mais próximo entre os nomes conhecidos.

Sua estratégia inclui:

- cores distintas para corpo, canhão, radar, bala e área de varredura;

- varreduras de 360 graus no loop principal;

- rotação do canhão até o ângulo observado do alvo;

- potência 3 para menos de 150 unidades, 2 para menos de 400 e 1 nas demais distâncias;

- aproximação ao inimigo quando não está fugindo: gira o corpo pelo bearing observado e avança `distância - 100`;

- movimento alternativo sem alvo: gira 45 graus e avança 80 unidades;

- modo de fuga por 20 ticks após ser atingido por bala, recuando 100 unidades e girando 45 graus a cada ciclo;

- recuo de 100 unidades e giro de 90 graus ao atingir a parede;

- remoção do inimigo morto do mapa e limpeza do alvo quando o robô eliminado era o alvo atual.

Assim como Ultron, a mira do Megatron usa a posição angular observada; o código não contém cálculo de interceptação baseado na velocidade do adversário.

## Executando no Robocode

O código deve ser usado em uma instalação do Robocode com a API Java disponível. Como os arquivos declaram o pacote `xcore`, eles precisam ser compilados/reconhecidos pelo Robocode respeitando esse package.

O repositório não registra um comando próprio de build ou uma versão específica do Robocode. Portanto, a forma de importar, compilar e iniciar uma batalha deve seguir a instalação e a versão do Robocode utilizada no ambiente local.

Depois de carregadas as classes, os robôs disponíveis são:

```
xcore.Xcore
xcore.Ultron
xcore.Megatron
```

## Materiais de apoio

A pasta `docs/` contém PDFs sobre instalação do Java e do Robocode, eventos e métodos de robôs, orientação a objetos e a classe `Robot`, além de métodos do canhão.

## Licença

O projeto está publicado sob a [MIT License](LICENSE). O arquivo de licença identifica Davi de Jesus como titular do copyright do repositório.
