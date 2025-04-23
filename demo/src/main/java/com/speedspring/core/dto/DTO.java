package com.speedspring.core.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface genérica para Data Transfer Objects (DTOs), permitindo conversão
 * entre DTOs e modelos de entidade (Model) através de reflexão.
 *
 * @param <M> O tipo do modelo associado ao DTO.
 * 
 * @author Corrêa
 */
public interface DTO<M> {

    /**
     * Retorna a classe do modelo associado a este DTO.
     *
     * @return a classe do modelo.
     */
    @JsonIgnore
    Class<M> getModelClass();

    /**
     * Inicializa os campos do DTO com base nos valores de um objeto de modelo fornecido.
     * Campos com o mesmo nome e tipo compatível serão copiados.
     *
     * @param classe o objeto de modelo a partir do qual os dados serão extraídos.
     */
    default void initBy(M classe) {
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
                }

            } catch (NoSuchFieldException e) {
                // Campo inexistente no DTO, ignora.
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Converte este DTO para uma instância do modelo associado,
     * utilizando a classe informada por {@link #getModelClass()}.
     *
     * @return instância do modelo com os dados do DTO ou {@code null} em caso de erro.
     */
    default M toModel() {
        try {
            Class<M> modelo = getModelClass();
            Constructor<M> constructor = modelo.getDeclaredConstructor();
            constructor.setAccessible(true);
            M model = constructor.newInstance();
            mapPropertiesUsingReflection(this, model);

            return model;
        } catch (Exception e) {
            System.err.println("Erro ao instanciar ou mapear model: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converte este DTO para uma instância de modelo da classe especificada.
     *
     * @param modelClass a classe do modelo.
     * @return instância do modelo com os dados do DTO ou {@code null} em caso de erro.
     */
    default M toModel(Class<M> modelClass) {
        try {
            Constructor<M> constructor = modelClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            M model = constructor.newInstance();
            mapPropertiesUsingReflection(this, model);

            return model;
        } catch (Exception e) {
            System.err.println("Erro ao instanciar ou mapear model: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mapeia campos de um objeto fonte para um objeto alvo utilizando reflexão,
     * incluindo campos das superclasses.
     *
     * @param source objeto de origem (DTO).
     * @param target objeto de destino (modelo).
     * @throws Exception caso ocorra erro durante o mapeamento.
     */
    default void mapPropertiesUsingReflection(Object source, M target) throws Exception {
        mapFieldsFromClass(source, target, source.getClass());

        Class<?> superClass = source.getClass().getSuperclass();
        while (superClass != null && superClass != Object.class) {
            mapFieldsFromClass(source, target, superClass);
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * Mapeia os campos de uma classe específica do objeto de origem para o objeto alvo.
     *
     * @param source objeto de origem.
     * @param target objeto de destino.
     * @param sourceClass classe de onde os campos devem ser lidos.
     * @throws Exception caso ocorra erro de acesso ou conversão.
     */
    default void mapFieldsFromClass(Object source, M target, Class<?> sourceClass) throws Exception {
        Field[] sourceFields = sourceClass.getDeclaredFields();

        for (Field sourceField : sourceFields) {
            try {
                sourceField.setAccessible(true);
                String fieldName = sourceField.getName();

                try {
                    if (!fieldName.equals("modelClass")) {
                        Field targetField = findField(target.getClass(), fieldName);
                        targetField.setAccessible(true);

                        if (targetField.getType().isAssignableFrom(sourceField.getType())) {
                            Object value = sourceField.get(source);
                            targetField.set(target, value);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // Campo inexistente no destino, ignora.
                } catch (IllegalAccessException e) {
                    System.out.println("Aviso: Não foi possível acessar o campo " + fieldName + " - " + e.getMessage());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erro ao processar campo: " + e.getMessage());
            }
        }
    }

    /**
     * Busca recursivamente um campo pelo nome em uma classe e suas superclasses.
     *
     * @param clazz classe onde a busca será realizada.
     * @param fieldName nome do campo a ser encontrado.
     * @return o campo encontrado.
     * @throws NoSuchFieldException se o campo não for encontrado em nenhuma das classes.
     */
    default Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
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
