package br.com.loja.service;

import br.com.loja.dto.CategoriaDTO;
import br.com.loja.model.Categoria;
import br.com.loja.repository.CategoriaRepository;
import br.com.loja.repository.ProdutoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private static final int TAMANHO_MAXIMO_NOME = 45;

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository) {

        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll(Sort.by("id"));
    }

    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }

    public Categoria cadastrar(CategoriaDTO dto) {
        String nome = validarNome(dto);

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe uma categoria com este nome.");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);

        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Integer id, CategoriaDTO dto) {
        String nome = validarNome(dto);
        Categoria categoria = buscarPorId(id);

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe uma categoria com este nome.");
        }

        categoria.setNome(nome);

        return categoriaRepository.save(categoria);
    }

    public void excluir(Integer id) {
        Categoria categoria = buscarPorId(id);

        // O banco não tem chave estrangeira entre produto e categoria,
        // então a regra é garantida aqui.
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta categoria possui produtos cadastrados e não pode ser excluída.");
        }

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Esta categoria está em uso e não pode ser excluída.");
        }
    }

    private String validarNome(CategoriaDTO dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O nome da categoria é obrigatório.");
        }

        String nome = dto.getNome().trim();

        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome da categoria deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }

        return nome;
    }
}
