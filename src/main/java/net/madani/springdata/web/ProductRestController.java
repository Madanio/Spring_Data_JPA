package net.madani.springdata.web;

import net.madani.springdata.entities.Product;
import net.madani.springdata.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductRestController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public List<Product> allProducts(){
        return productRepository.findAll();
    }

    @GetMapping("/products/{id}")
    public Product findById(@PathVariable Long id){
        return productRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Product not found"));
    }

    @GetMapping("/find/products")
    public List<Product> findProducts(@RequestParam(name = "kw", defaultValue = "") String keyword){
        return productRepository.findByNameContains(keyword);
    }

    @PostMapping("/products")
    public Product save(@RequestBody Product product){
        return productRepository.save(product);
    }

    @DeleteMapping("/products/{id}")
    public void delete(@PathVariable Long id){
        productRepository.deleteById(id);
    }
}
