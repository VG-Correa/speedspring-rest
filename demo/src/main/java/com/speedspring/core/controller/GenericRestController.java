package com.speedspring.core.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.speedspring.core.dtoConvertable.DTOConvertable;
import com.speedspring.core.exception.EntityAlreadActiveException;
import com.speedspring.core.exception.EntityAlreadExistsException;
import com.speedspring.core.exception.EntityAlreadyDeletedException;
import com.speedspring.core.exception.EntityInvalidException;
import com.speedspring.core.exception.EntityNotFoundException;
import com.speedspring.core.responses.GenericResponse;
import com.speedspring.core.responses.InternalErrorResponse;

/**
 * Controlador REST genérico que centraliza a criação de respostas HTTP padronizadas
 * para operações CRUD e tratamento de exceções.
 * <p>
 * Este controlador oferece métodos utilitários para:
 * <ul>
 *   <li>Gerar respostas de sucesso com diferentes códigos HTTP (200, 201);</li>
 *   <li>Gerar respostas de deleção com backup de dados;</li>
 *   <li>Gerar respostas de erro interno (500);</li>
 *   <li>Gerar respostas específicas para exceções customizadas, como:<br>
 *       {@link EntityAlreadExistsException}, {@link EntityAlreadyDeletedException},<br>
 *       {@link EntityAlreadActiveException}, {@link EntityNotFoundException},<br>
 *       {@link EntityInvalidException}.</li>
 *   <li>Converter coleções de entidades em listas de DTOs via {@link DTOConvertable}.</li>
 * </ul>
 * <p>
 * Todas as respostas seguem o padrão {@link GenericResponse}, garantindo consistência
 * no formato de retorno da API (status, mensagem, dados, erro e metadados).
 * <p>
 * Exemplo de uso em serviço REST:
 * <pre>
 * &#64;GetMapping("/usuarios")
 * public ResponseEntity<GenericResponse> listarUsuarios() {
 *     List<Usuario> usuarios = usuarioService.obterTodos();
 *     List<UsuarioDTO> dtos = toDTO(usuarios);
 *     return getResponseOK("Usuários carregados com sucesso", dtos);
 * }
 * </pre>
 *
 * @author VG-Correa
 * @since 1.0
 */
@RestController
public class GenericRestController {

    /**
     * Instância de resposta genérica injetada pelo Spring, utilizada como base
     * para montagem de todas as respostas HTTP.
     */
    @Autowired
    private GenericResponse response;

    /**
     * Constrói e retorna a {@link ResponseEntity} padrão com o corpo e status definidos
     * em {@link #response}.
     *
     * @return ResponseEntity contendo o {@link GenericResponse} configurado
     */
    public ResponseEntity<GenericResponse> getResponse() {
        return ResponseEntity.status(response.getStatus())
                             .body(response.build());
    }

    /**
     * Retorna um response 201 (Created) com mensagem, dados e metadados.
     *
     * @param message Mensagem de confirmação da criação
     * @param data    Objeto de dados criado ou persistido
     * @param metadata Informações adicionais (ex.: IDs, URLs)
     * @return ResponseEntity HTTP 201
     */
    public ResponseEntity<GenericResponse> getResponseCreated(String message,
                                                              Object data,
                                                              Object metadata) {
        response.setStatus(201)
                .setError(false)
                .setMessage(message)
                .setData(data)
                .setMetadata(metadata);
        return getResponse();
    }

    /**
     * Retorna um response 200 (OK) para operação de deleção com backup dos dados removidos.
     *
     * @param message Mensagem de confirmação da deleção
     * @param backup  Estado anterior do recurso removido
     * @return ResponseEntity HTTP 200
     */
    public ResponseEntity<GenericResponse> getResponseDeleted(String message,
                                                              Object backup) {
        response.setStatus(200)
                .setError(false)
                .setMessage(message)
                .setMetadata(Map.of("backup", backup));
        return getResponse();
    }

    /**
     * Retorna um response 201 (Created) com mensagem e dados.
     *
     * @param message Mensagem de confirmação da criação
     * @param data    Objeto de dados criado ou persistido
     * @return ResponseEntity HTTP 201
     */
    public ResponseEntity<GenericResponse> getResponseCreated(String message,
                                                              Object data) {
        response.setStatus(201)
                .setError(false)
                .setMessage(message)
                .setData(data);
        return getResponse();
    }

    /**
     * Retorna um response 200 (OK) com mensagem, dados e metadados.
     *
     * @param message  Mensagem de sucesso
     * @param data     Dados retornados pela operação
     * @param metadata Informações adicionais complementares
     * @return ResponseEntity HTTP 200
     */
    public ResponseEntity<GenericResponse> getResponseOK(String message,
                                                          Object data,
                                                          Object metadata) {
        response.setStatus(200)
                .setError(false)
                .setMessage(message)
                .setData(data)
                .setMetadata(metadata);
        return getResponse();
    }

    /**
     * Retorna um response 200 (OK) apenas com mensagem.
     *
     * @param message Mensagem de sucesso
     * @return ResponseEntity HTTP 200
     */
    public ResponseEntity<GenericResponse> getResponseOK(String message) {
        response.setStatus(200)
                .setError(false)
                .setMessage(message);
        return getResponse();
    }

    /**
     * Retorna um response 200 (OK) com mensagem e dados.
     *
     * @param message Mensagem de sucesso
     * @param data    Dados retornados pela operação
     * @return ResponseEntity HTTP 200
     */
    public ResponseEntity<GenericResponse> getResponseOK(String message,
                                                          Object data) {
        response.setStatus(200)
                .setError(false)
                .setMessage(message)
                .setData(data);
        return getResponse();
    }

    /**
     * Gera uma resposta de erro interno do servidor (HTTP 500) baseada em uma exceção.
     *
     * @param e Exceção que causou o erro
     * @return ResponseEntity HTTP 500
     */
    public ResponseEntity<GenericResponse> getResponseInternalError(Exception e) {
        response = new InternalErrorResponse(e);
        return getResponse();
    }

    /**
     * Gera uma resposta de erro interno do servidor (HTTP 500) padrão.
     *
     * @return ResponseEntity HTTP 500
     */
    public ResponseEntity<GenericResponse> getResponseInternalError() {
        response = new InternalErrorResponse(new Exception("Erro interno do servidor"));
        return getResponse();
    }

    /**
     * Gera resposta 409 (Conflict) para exceção de entidade já existente.
     *
     * @param e Exceção lançada
     * @return ResponseEntity HTTP 409
     */
    public ResponseEntity<GenericResponse> getResponseEntityExistsException(EntityAlreadExistsException e) {
        response.setStatus(409)
                .setError(true)
                .setMessage(e.getMessage());
        return getResponse();
    }

    /**
     * Gera resposta 409 (Conflict) para exceção de entidade já deletada.
     *
     * @param e Exceção lançada
     * @return ResponseEntity HTTP 409
     */
    public ResponseEntity<GenericResponse> getResponseEntityAlreadyDeletedException(EntityAlreadyDeletedException e) {
        response.setStatus(409)
                .setError(true)
                .setMessage(e.getMessage());
        return getResponse();
    }

    /**
     * Gera resposta 400 (Bad Request) para exceção de entidade já ativa.
     *
     * @param e Exceção lançada
     * @return ResponseEntity HTTP 400
     */
    public ResponseEntity<GenericResponse> getResponseEntityAlreadyActiveException(EntityAlreadActiveException e) {
        response.setStatus(400)
                .setError(true)
                .setMessage(e.getMessage());
        return getResponse();
    }

    /**
     * Gera resposta 404 (Not Found) para exceção de entidade não encontrada.
     *
     * @param e Exceção lançada
     * @return ResponseEntity HTTP 404
     */
    public ResponseEntity<GenericResponse> getResponseNotFound(EntityNotFoundException e) {
        response.setStatus(404)
                .setError(true)
                .setMessage(e.getMessage());
        return getResponse();
    }

    /**
     * Gera resposta 400 (Bad Request) para exceção de entidade inválida.
     *
     * @param e Exceção lançada
     * @return ResponseEntity HTTP 400
     */
    public ResponseEntity<GenericResponse> getResponseInvalidEntity(EntityInvalidException e) {
        response.setStatus(400)
                .setError(true)
                .setMessage(e.getMessage());
        return getResponse();
    }

    /**
     * Trata exceções genéricas e direciona para o método apropriado de resposta.
     * Caso não seja uma das exceções customizadas, retorna erro interno (500).
     *
     * @param e Exceção capturada
     * @return ResponseEntity com código HTTP correspondente
     */
    public ResponseEntity<GenericResponse> getResponseException(Exception e) {
        switch (e) {
            case EntityAlreadExistsException ex ->   { return getResponseEntityExistsException(ex); }
            case EntityAlreadyDeletedException ex -> { return getResponseEntityAlreadyDeletedException(ex); }
            case EntityNotFoundException ex ->      { return getResponseNotFound(ex); }
            case EntityInvalidException ex ->       { return getResponseInvalidEntity(ex); }
            case EntityAlreadActiveException ex ->  { return getResponseEntityAlreadyActiveException(ex); }
            default ->                              { return getResponseInternalError(e); }
        }
    }

    /**
     * Converte uma lista de objetos que implementam {@link DTOConvertable}
     * em uma lista de seus respectivos DTOs.
     *
     * @param <T>            Tipo do DTO de saída
     * @param dtoConvertable Lista de entidades ou wrappers DTOConvertable
     * @return Lista de DTOs convertidos
     */
    public <T> List<T> toDTO(List<? extends DTOConvertable<?, T>> dtoConvertable) {
        return dtoConvertable.stream()
                             .map(dto -> dto.toDTO())
                             .collect(Collectors.toList());
    }
}
