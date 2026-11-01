# Gate de integração com o UniTVnet original — evidência estática

**Fontes:** APK TV 1.10.0 e APK Mobile 1.11.0 fornecidos pelo usuário. Ferramenta `tools/manifest_probe.py`, auditoria read-only de `AndroidManifest.xml` e DEX. Não acessa dados de conta nem servidores.

## Resultado

| Campo | TV | Mobile |
|---|---|---|
| Pacote | `com.unitvnet.tvod` | `com.unitvnet.mobs` |
| Aplicação protegida | `com.secneo.apkwrapper.AW` | `com.secneo.apkwrapper.AW` |
| Activities | 34 | 72 |
| DEX principal, classes declaradas | 7 | 11 |
| Activity de player exportada no manifesto | Não identificada | Não: `com.mobile.brasiltv.activity.PlayAty` não exportada |
| PiP declarado no player | Não identificado | Não |
| Launcher | `WelcomeActivity` (TV/Leanback) | `SplashAty` (Mobile) |

Os originais também apresentam **nomes de pacote distintos**, e os certificados JAR/V1 foram verificados via `keytool` com impressão SHA-256 comum: `DD:D6:60:81:1E:5B:C2:C5:B7:F6:24:44:81:1A:38:B6:D9:E9:95:EF:64:8A:69:FF:BA:52:0C:1D:E8:4D:E9:5F`. Não temos a chave privada de assinatura.

### Interpretação

1. Não se pode simplesmente juntar dois manifestos/APKs e obter um aplicativo autenticado. É necessária uma única aplicação compilada com acesso legítimo às funcionalidades.
2. Não há **ponte externa de player comprovada** pelo manifesto; a ausência não exclui mecanismos internos em tempo de execução. Deep links de lançamento não são prova de acesso direto ao VOD.
3. O PiP do Android exige integração com a Activity de reprodução e seu ciclo de vida. Alterar somente a flag do manifesto não é suficiente. Fonte: https://developer.android.com/develop/ui/views/picture-in-picture
4. MediaSession e Watch Next exigem eventos/metadados reais do player e roteamento de retomada. Na Google TV, Watch Next está sujeito à certificação do Google.
5. O wrapper Secneo está confirmado; os layouts recuperados não representam código de lógica, autenticação ou reprodução.

### Resultado do gate

**NÃO APROVADO** para a entrega de um APK híbrido com equivalência funcional. Faltam: player original controlável e testado, preservação da autenticação original, fluxo real de catálogo/VOD, validação instrumentada em dispositivos. Não criar player fictício para encobrir a ausência desses requisitos.

### Uso da auditoria

```bash
python tools/manifest_probe.py --tv /caminho/UniTVnet_1.10.0.apk --mobile /caminho/UniTVnet_1.11.0.apk --output /tmp/unitv-manifest.json
python -m unittest discover -s tools/tests -v
```

Os APKs originais não são armazenados neste repositório público. Resultados da CI sem os arquivos originais têm os testes específicos ignorados; execute-os localmente com os APKs disponibilizados pelo usuário.
