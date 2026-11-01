# UniTVnet — estudo de fidelidade dos APKs originais

Este diretório existe para **engenharia reversa do UniTVnet original**, não para criar um concorrente ou um reprodutor genérico.

- [Regras de foco](FOCO.md)
- [Evidências da interface extraídas dos APKs](RELATORIO_EVIDENCIAS.md)
- `tools/apk_baseline.py`: auditoria local reprodutível, sem alterar originais.
- `tools/arsc_resources.py`: indexa IDs e nomes de recursos de `resources.arsc` dos APKs originais.
- `tools/axml_layout.py`: decodifica a hierarquia de XML binário original, como `aty_main`, `frag_home` e `aty_play`.
- `tools/tests/test_resource_parsers.py`: testes do decodificador; testes com APK original são locais e não fazem parte da CI pública.
- `tools/tests/test_apk_baseline.py`: testes com APK sintético.

Execute com **cópias autorizadas dos APKs**, fora do Git:

```bash
python3 tools/apk_baseline.py --tv /caminho/UniTVnet_1.10.0.apk --mobile /caminho/UniTVnet_1.11.0.apk
python3 tools/arsc_resources.py /caminho/UniTVnet_1.11.0.apk --output /tmp/mobile-resources.json
python3 tools/axml_layout.py /caminho/UniTVnet_1.11.0.apk aty_main
python3 -m unittest discover -s tools/tests -v
```

**Nenhum APK refeito está pronto.** A fonte original está protegida por Secneo. As integrações de catálogo, login e player não foram recuperadas e testadas. Não publicar APKs, endereços do serviço, chaves nem credenciais.
