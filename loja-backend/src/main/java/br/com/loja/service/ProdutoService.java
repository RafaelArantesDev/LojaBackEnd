package br.com.loja.service;

import br.com.loja.dto.ProdutoCategoriaDTO;
import br.com.loja.dto.ProdutoDTO;
import br.com.loja.model.Categoria;
import br.com.loja.model.Produto;
import br.com.loja.repository.CategoriaRepository;
import br.com.loja.repository.ProdutoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProdutoService {

    private static final int TAMANHO_MAXIMO_NOME = 40;

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository) {

        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProdutoCategoriaDTO> buscarProdutosComCategoria() {
        return produtoRepository.buscarProdutosComCategoria();
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    public Produto cadastrar(ProdutoDTO dto) {
        validarDados(dto);

        Produto produto = new Produto();
        preencher(produto, dto);

        return produtoRepository.save(produto);
    }

    public Produto atualizar(Integer id, ProdutoDTO dto) {
        validarDados(dto);

        Produto produto = buscarPorId(id);
        preencher(produto, dto);

        return produtoRepository.save(produto);
    }

    public void excluir(Integer id) {
        Produto produto = buscarPorId(id);

        try {
            produtoRepository.delete(produto);
            produtoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este produto já foi vendido em algum pedido e não pode ser excluído.");
        }
    }

    private void preencher(Produto produto, ProdutoDTO dto) {
        produto.setNome(dto.getNome().trim());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        produto.setCategoria(buscarCategoria(dto.getCategoriaId()));
    }

    private Categoria buscarCategoria(Integer categoriaId) {
        if (categoriaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A categoria é obrigatória.");
        }

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Categoria não encontrada."));
    }

    private void validarDados(ProdutoDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Os dados do produto são obrigatórios.");
        }

        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O nome do produto é obrigatório.");
        }

        if (dto.getNome().trim().length() > TAMANHO_MAXIMO_NOME) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do produto deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }

        if (dto.getPreco() == null || dto.getPreco().signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O preço deve ser maior que zero.");
        }

        if (dto.getEstoque() == null || dto.getEstoque() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O estoque não pode ser negativo.");
        }
    }
}
