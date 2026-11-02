# Lvr Replica — reinício limpo da engenharia de interface Android

Este projeto começa do zero com base **observável** (capturas e navegação permitida no próprio aplicativo), seguindo o fluxo Replica: `recon → architect → design → build → test → diff`.

**Não é o UniTVnet original, não substitui os APKs fornecidos, não fornece IPTV ou catálogo e não aceita a conta UniTVnet.** A diferença não será ocultada. O projeto público não contém APKs originais ou dados de usuário.

## Primeiro incremento

- Entrada Mobile e TV no mesmo applicationId, reconhecidas com `UiModeManager` e `FEATURE_LEANBACK`.
- Configuração persistente para alternar os modos.
- Navegação real da estrutura superior e inferior com estados locais, sem botões de reprodução fictícios.
- Tela inicial baseada na composição de S01, com identificação explícita de dados ausentes.
- Sem permissão de rede ou reprodução fictícia.
- Testes de decisão de modo e ausência de conteúdo inventado.

## Executar testes

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Ambiente sem Gradle instalado: use o workflow de GitHub Actions. Testes Java independentes de Android:

```bash
javac -d /tmp/lvr-test app/src/main/java/br/com/lever/replica/ModeResolver.java app/src/main/java/br/com/lever/replica/ScreenState.java tools/SmokeContract.java
java -cp /tmp/lvr-test SmokeContract
```

## Referências

- `replica/recon.md`: inventário de telas, componentes e nível de evidência.
- `replica/features.csv`: matriz ponderada de paridade — nunca marcar conteúdo ausente como concluído.
- `replica/architecture.md`: decisões de engenharia.

Qualquer APK produzido nesta fase é **uma ferramenta de validação de UI**, não entrega do serviço funcional. O APK final solicitado ainda depende de paridade de catálogo/autenticação/reprodução, o que exige integração autorizada.
