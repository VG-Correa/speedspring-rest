# SpeedSpring Rest

**Versão:** 1.0.0\
**Autor:** Victor Gabriel Corrêa (@VG-Correa)

SpeedSpring Rest é uma biblioteca Java para facilitar a construção de APIs REST com Spring Boot, oferecendo um modelo de resposta padronizado e conversão automática entre entidades e DTOs com reflexão.

---

## ⚡ Instalação

Adicione a dependência no seu `pom.xml`:

```xml
<dependency>
    <groupId>com.speedspring</groupId>
    <artifactId>speedspring-rest</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🔧 Funcionalidades

- Conversão automática de entidades em DTOs com reflexão.
- Suporte a coleções e objetos aninhados.
- Respostas padronizadas com estrutura `GenericResponse`.
- Controller genérico com métodos reutilizáveis para respostas HTTP.
- Logging colorido e detalhado (ANSI).

---

## 🌐 Exemplo de Uso

### 1. Implementando `DTOConvertable`

```java
public class Usuario implements DTOConvertable<UsuarioWriteDTO, UsuarioDTO> {
    private String nome;
    private int idade;

    public Usuario() {}

    public Usuario(UsuarioWriteDTO writeDTO) {
        initBy(writeDTO);
    }

    @Override
    public Class<UsuarioDTO> getDTOClass() {
        return UsuarioDTO.class;
    }

    @Override
    public boolean getLog() {
        return true; // Ativa logs no console
    }
}
```

### 2. Controller com `GenericRestController`

```java
@RestController
@RequestMapping("/usuarios")
public class UsuarioController extends GenericRestController {

    @PostMapping
    public ResponseEntity<GenericResponse> criar(@RequestBody UsuarioWriteDTO dto) {
        Usuario usuario = new Usuario(dto);
        // salvar no banco (exemplo): usuarioRepository.save(usuario);

        return getResponseCreated("Usuário criado com sucesso", usuario.toDTO());
    }

    @GetMapping
    public ResponseEntity<GenericResponse> listar() {
        List<Usuario> lista = List.of(new Usuario("João", 30), new Usuario("Ana", 25));
        return getResponseOK("Listagem de usuários", toDTO(lista));
    }
}
```

---

## 📄 GenericResponse

Classe utilitária para encapsular qualquer resposta retornada pelos controllers.

```json
{
  "status": 200,
  "error": false,
  "message": "Operação realizada com sucesso",
  "data": [ ... ],
  "metadata": { ... }
}
```

Você pode usar os métodos como:

```java
getResponseOK("Sucesso", objeto);
getResponseCreated("Criado", objeto);
getResponseDeleted("Removido", backup);
getResponseException(exception);
```

---

## ✨ Conversão Reflexiva

A interface `DTOConvertable` permite que você converta automaticamente objetos para DTOs com:

```java
UsuarioDTO dto = usuario.toDTO();
```

Ou para uma classe específica:

```java
UsuarioDTO dto = usuario.toDTO(UsuarioDTO.class);
```

---

## 🔧 Dependências

- Spring Boot Starter Web
- Spring Boot Starter Data REST
- Spring Boot Starter Actuator
- Apache Commons JEXL
- Exp4j 

---

## ✅ Contribuições

Pull requests são bem-vindos. Para grandes mudanças, abra uma issue primeiro para discutirmos o que você quer modificar.

---

## ✉ Contato

Victor Gabriel Corrêa\
Email: [v.victorgabriel2014@gmail.com](mailto\:v.victorgabriel2014@gmail.com)\
GitHub: [VG-Correa](https://github.com/VG-Correa)

---

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para mais detalhes.

