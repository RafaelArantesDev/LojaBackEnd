package br.com.loja.repository;

import br.com.loja.dto.ProdutoCategoriaDTO;
import br.com.loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @Query("""
            SELECT new br.com.loja.dto.ProdutoCategoriaDTO(
                p.id,
                p.nome,
                p.preco,
                p.estoque,
                c.id,
                c.nome
            )
            FROM Produto p
            LEFT JOIN p.categoria c
            ORDER BY p.id
            """)
    List<ProdutoCategoriaDTO> buscarProdutosComCategoria();

    // Usado para impedir a exclusão de categorias que ainda têm produtos
    boolean existsByCategoriaId(Integer categoriaId);
}
