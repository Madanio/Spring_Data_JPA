package net.madani.springdata;

import net.madani.springdata.entities.Product;
import net.madani.springdata.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class DemoBdccSpringDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBdccSpringDataApplication.class, args);
	}

	@Bean
	CommandLineRunner start(ProductRepository productRepository) {
		return args -> {
			// Ajouter des produits
			Product p1 = new Product();
			p1.setName("Computer");
			p1.setPrice(3400);
			p1.setQuantity(2);
			Product p2 = new Product(null, "Printer", 1200, 13);
			Product p3 = Product.builder()
					.name("Smart phone").price(1300).quantity(4)
					.build();
			productRepository.save(p1);
			productRepository.save(p2);
			productRepository.save(p3);

			// Consulter tous les produits
			List<Product> products = productRepository.findAll();
			products.forEach(p -> {
				System.out.println(p.toString());
			});

			// Consulter un produit
			Product product = productRepository.findById(1L).orElse(null);
			System.out.println("Produit 1 : " + product);

			// Chercher des produits
			List<Product> found = productRepository.findByNameContains("C");
			found.forEach(System.out::println);

			// Mettre à jour un produit
			if (product != null) {
				product.setPrice(3000);
				productRepository.save(product);
			}

			// Supprimer un produit
			productRepository.deleteById(3L);
		};
	}

}
