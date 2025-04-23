package com.spec.speedspring.core.dtoConvertable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface para conversão reflexiva entre objetos de domínio (model) e DTOs (Data Transfer Objects).
 * <p>
 * Esta interface provê métodos que permitem:
 * <ul>
 *   <li>Iniciar um DTO a partir de uma instância de domínio (<code>initBy</code>);</li>
 *   <li>Converter um objeto de domínio em seu DTO correspondente (<code>toDTO</code>);</li>
 *   <li>Mapear propriedades recursivamente, incluindo coleções e objetos aninhados, usando reflexão.</li>
 * </ul>
 * <p>
 * Recursos adicionais:
 * <ul>
 *   <li>Logging detalhado, com saídas coloridas via ANSI no console, controlado pelo método <code>getLog()</code>;</li>
 *   <li>Tratamento automático de coleções (<code>List</code>, <code>Set</code> e demais <code>Collection</code>), convertendo cada item que implemente <code>DTOConvertable</code>;</li>
 *   <li>Busca recursiva de campos em superclasses para maior compatibilidade de modelos hierárquicos.</li>
 * </ul>
 *
 * <h3>Exemplo de uso</h3>
 * <pre>
 * public class Usuario implements DTOConvertable<UsuarioWriteDTO, UsuarioDTO> {
 *     private String nome;
 *     private Integer idade;
 *     // getters, setters...
 *
 *    // Construtor padrão opcional para reflexão
 *     public Usuario(UsuarioWriteDTO usuarioWriteDTO) {
 *          initBy(usuarioWriteDTO);
 *     }
 * 
 *     @Override
 *     public Class<UsuarioDTO> getDTOClass() {
 *         return UsuarioDTO.class; // classe do DTO correspondente
 *     }
 *
 *     @Override
 *     public boolean getLog() {
 *         return true; // ativa logs no console
 *     }
 * }
 *
 *
 * <h3>Exemplo de uso Em RestController</h3>
 * <pre>
 * @RestController
 * @RequestMapping("/usuarios")
 * public class UsuarioRestController extends GenericRestController {
 * 
 *     @PostMapping
 *     public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody UsuarioWriteDTO usuarioWriteDTO) {
 *         Usuario usuario = new Usuario();
 *         usuario.initBy(usuarioWriteDTO); // Inicializa o objeto de domínio com os dados do DTO de entrada
 *         
 *         // ou passando direto o WriteDTO no construtor se o construtor estiver implementado
 *         // Usuario usuario = new Usuario(usuarioWriteDTO);
 *  
 *         // Aqui você pode salvar o objeto usuario no banco de dados, se necessário
 *          
 * 
 * 
 *         UsuarioDTO usuarioDTO = usuario.toDTO(); // Converte o objeto de domínio para o DTO de saída
 *         return getResponseOK("Usuario convertido com sucesso", (usuarioDTO));
 *     }
 * }
 *
 * 
 * </pre>
 *
 * @param <W> Tipo do objeto de domínio (Model) a ser convertido
 * @param <R> Tipo do DTO resultante da conversão
 * @author VG-Correa
 */
public interface DTOConvertable<W, R> {

    /**
     * Retorna a classe do DTO que será instanciada durante a conversão.
     * <p>
     * Este método é ignorado em serialização JSON.
     *
     * @return classe do DTO correspondente
     */
    @JsonIgnore
    Class<R> getDTOClass();

    /**
     * Indica se as operações de conversão/reflexão devem gerar logs no console.
     * <p>
     * Quando <code>true</code>, várias informações de debug serão impressas,
     * incluindo nomes de campos, tipos e valores antes e depois da cópia.
     *
     * @return <code>true</code> para ativar logs; <code>false</code> para modo silencioso
     */
    boolean getLog();

    /**
     * Copia valores dos campos de uma instância de domínio (<code>W</code>)
     * para a instância atual (DTOConvertable).
     * <p>
     * Apenas campos com mesmo nome e tipo compatível são copiados. Campos não encontrados
     * são ignorados silenciosamente (ou logados, se <code>getLog()</code> estiver ativo).
     *
     * @param classe instância de domínio que serve de origem para os dados
     */
    default void initBy(W classe) {
        if (getLog()) {
            System.out.println("[DTOConvertable] Iniciando initBy para: " + classe.getClass().getSimpleName());
        }

        Field[] fields = classe.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Field targetField = findField(getClass(), fieldName);
                targetField.setAccessible(true);

                if (targetField.getType().isAssignableFrom(field.getType())) {
                    Object value = field.get(classe);
                    targetField.set(this, value);
                    if (getLog()) {
                        System.out.println("[DTOConvertable] Campo copiado: " + fieldName + " = " + value);
                    }
                }
            } catch (NoSuchFieldException e) {
                if (getLog()) {
                    System.out.println("[DTOConvertable] Campo não encontrado no target: " + fieldName);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                if (getLog()) {
                    System.out.println("[DTOConvertable] Erro ao copiar campo " + fieldName + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Cria e retorna uma instância de DTO (<code>R</code>) a partir do objeto atual.
     * <p>
     * O método utiliza o construtor padrão do DTO e chama
     * <code>mapPropertiesUsingReflection</code> para copiar todos os campos.
     *
     * @return objeto DTO populado, ou <code>null</code> em caso de erro de reflexão
     */
    default R toDTO() {
        if (getLog()) {
            System.out.println("[DTOConvertable] Convertendo para DTO usando getDTOClass");
            System.out.println("[DTOConvertable: " + this.getClass().getSimpleName() + "] -> [" + getDTOClass().getSimpleName() + "]");
        }
        try {
            Class<R> dtoClass = getDTOClass();
            Constructor<R> constructor = dtoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            R dto = constructor.newInstance();
            mapPropertiesUsingReflection(this, dto);
            if (getLog()) {
                System.out.println("[DTOConvertable] Conversão concluída com sucesso: " + dtoClass.getSimpleName());
            }
            return dto;
        } catch (Exception e) {
            System.err.println("[DTOConvertable] Erro ao instanciar ou mapear DTO: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cria e retorna uma instância de DTO (<code>R</code>) usando a classe fornecida.
     * <p>
     * Útil quando deseja converter para uma subclasse específica de DTO.
     *
     * @param dtoClass classe do DTO desejado
     * @return objeto DTO populado, ou <code>null</code> em caso de erro
     */
    default R toDTO(Class<R> dtoClass) {
        if (getLog()) {
            System.out.println("[DTOConvertable] Convertendo para DTO com classe fornecida: " + dtoClass.getSimpleName());
            System.out.println("[DTOConvertable: " + this.getClass().getSimpleName() + "] -> [" + dtoClass.getSimpleName() + "]");
        }
        try {
            Constructor<R> constructor = dtoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            R dto = constructor.newInstance();
            mapPropertiesUsingReflection(this, dto);
            if (getLog()) {
                System.out.println("[DTOConvertable] Conversão concluída com sucesso: " + dtoClass.getSimpleName());
            }
            return dto;
        } catch (Exception e) {
            System.err.println("[DTOConvertable] Erro ao instanciar ou mapear DTO: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mapeia todos os campos do objeto fonte para o objeto destino utilizando reflexão, incluindo superclasses.
     *
     * @param source objeto fonte (entity ou DTOConvertable anterior)
     * @param target objeto destino (DTO)
     * @throws Exception em caso de erro de acesso/reflexão de campos
     */
    default void mapPropertiesUsingReflection(Object source, R target) throws Exception {
        if (getLog()) {
            System.out.println("[DTOConvertable] Iniciando mapeamento por reflexão...");
        }
        mapFieldsFromClass(source, target, source.getClass());
        Class<?> superClass = source.getClass().getSuperclass();
        while (superClass != null && superClass != Object.class) {
            mapFieldsFromClass(source, target, superClass);
            superClass = superClass.getSuperclass();
        }
        if (getLog()) {
            System.out.println("[DTOConvertable] Mapeamento completo.");
        }
    }

    /**
     * Realiza o mapeamento detalhado de campos de uma classe específica para o DTO.
     * Suporta:
     * <ul>
     *   <li>Objetos aninhados que implementam DTOConvertable;</li>
     *   <li>Coleções de objetos DTOConvertable;</li>
     *   <li>Atribuição direta de tipos compatíveis;</li>
     *   <li>Logs coloridos via ANSI para informações de debug.</li>
     * </ul>
     *
     * @param source      objeto fonte
     * @param target      objeto destino (DTO)
     * @param sourceClass classe a ser mapeada nesta iteração
     * @throws Exception em caso de erro durante leitura ou escrita de campos
     */
    default void mapFieldsFromClass(Object source, R target, Class<?> sourceClass) throws Exception {
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        final String RED = "\u001B[31m";
        final String PURPLE = "\u001B[35m";

        Field[] sourceFields = sourceClass.getDeclaredFields();
        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            String fieldName = sourceField.getName();
            try {
                if (!fieldName.equals("modelClass")) {
                    Field targetField = findField(target.getClass(), fieldName);
                    targetField.setAccessible(true);
                    Object value = sourceField.get(source);
                    if (value == null) continue;
                    if (getLog()) {
                        System.out.println(PURPLE + "\n[DTOConvertable] Mapeando campo: " + fieldName + RESET);
                        System.out.println(" ├── Tipo Origem : " + CYAN + sourceField.getType().getSimpleName() + RESET);
                        System.out.println(" ├── Tipo Destino: " + CYAN + targetField.getType().getSimpleName() + RESET);
                        System.out.println(" ├── Valor antes : " + YELLOW + value + RESET);
                    }

                    if (value instanceof DTOConvertable<?, ?> dtoValue) {
                        Object dtoConverted = dtoValue.toDTO();
                        targetField.set(target, dtoConverted);
                        if (getLog()) {
                            System.out.println(" └── " + GREEN + "DTO Interno convertido com sucesso" + RESET);
                            System.out.println("     └── Valor depois: " + GREEN + dtoConverted + RESET);
                        }
                    } else if (value instanceof Collection<?> collection) {
                        Collection<Object> dtoCollection = value instanceof List ? new ArrayList<>() :
                                                           value instanceof Set ? new HashSet<>() :
                                                           new ArrayList<>();
                        for (Object item : collection) {
                            if (item instanceof DTOConvertable<?, ?> itemDto) {
                                dtoCollection.add(itemDto.toDTO());
                            } else {
                                dtoCollection.add(item);
                            }
                        }
                        targetField.set(target, dtoCollection);
                        if (getLog()) {
                            System.out.println(" └── " + GREEN + "Coleção de DTOs convertida com sucesso" + RESET);
                            System.out.println("     └── Itens convertidos: " + GREEN + dtoCollection + RESET);
                        }
                    } else if (targetField.getType().isAssignableFrom(sourceField.getType())) {
                        targetField.set(target, value);
                        if (getLog()) {
                            System.out.println(" └── " + GREEN + "Campo copiado diretamente" + RESET);
                        }
                    } else {
                        if (getLog()) {
                            System.out.println(" └── " + RED + "Tipo incompatível, campo ignorado" + RESET);
                        }
                    }
                }
            } catch (NoSuchFieldException e) {
                if (getLog()) {
                    System.out.println("[DTOConvertable] Campo ignorado (não existe no DTO): " + fieldName);
                }
            } catch (IllegalAccessException e) {
                System.out.println("[DTOConvertable] Aviso: Não foi possível acessar o campo " + fieldName + " - " + e.getMessage());
            }
        }
    }

    /**
     * Procura recursivamente um campo pelo nome em uma classe e suas superclasses.
     * Campos chamados "modelClass" são ignorados por projeto.
     *
     * @param clazz     classe onde a busca inicia
     * @param fieldName nome do campo procurado
     * @return Field encontrado
     * @throws NoSuchFieldException se não existir em clazz nem em superclasses
     */
    default Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        if (fieldName.equals("modelClass")) {
            throw new NoSuchFieldException("Campo 'modelClass' ignorado propositalmente");
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, fieldName);
            }
            throw e;
        }
    }
}
