# Spring Data JPA — Gestion des produits

Application Spring Boot qui gère des produits avec **Spring Data JPA** et une base **H2** en mémoire, puis les expose dans un **web service REST** documenté avec **Swagger**.

D'après la vidéo de Pr. Mohamed YOUSSFI.

**Technologies :** Java 21, Spring Boot 3.5.16, Spring Data JPA, Hibernate, H2, Lombok, Spring Web, springdoc-openapi 2.8.17, Maven.

## Lancer le projet

```bash
./mvnw spring-boot:run        # Windows : mvnw.cmd spring-boot:run
```

L'application démarre sur le port **8084**.

| URL | Rôle |
|---|---|
| http://localhost:8084/h2-console | console H2 (JDBC URL `jdbc:h2:mem:products-db`, user `sa`, sans mot de passe) |
| http://localhost:8084/swagger-ui.html | Swagger UI |
| http://localhost:8084/v3/api-docs | description OpenAPI (JSON) |

## Structure du projet

```
src/main/java/net/madani/springdata
├── DemoBdccSpringDataApplication.java   // main + CommandLineRunner (tests CRUD)
├── entities/Product.java                // entité JPA
├── repository/ProductRepository.java    // interface Spring Data
└── web/ProductRestController.java       // web service REST
src/main/resources/application.properties
```

## 1. Architecture

```
Spring Data JPA  →  JPA  →  Hibernate  →  JDBC  →  H2
```

- **Spring Data JPA** : on écrit seulement une interface ; Spring génère l'implémentation (CRUD + requêtes déduites du nom des méthodes).
- **JPA** (Jakarta Persistence) : la spécification standard du mapping objet-relationnel (annotations `@Entity`, `@Id`, …).
- **Hibernate** : l'implémentation de JPA utilisée par Spring Boot ; il génère le SQL et crée les tables.
- **JDBC** : l'API Java bas niveau qui envoie le SQL à la base.
- **H2** : base de données relationnelle en mémoire, recréée à chaque démarrage.

## 2. Entité `Product`

```java
@Entity
@Table(name = "PRODUCTS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString @Builder
public class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private double price;
    private int quantity;
}
```

| Annotation | Rôle |
|---|---|
| `@Entity` | la classe est persistante : elle correspond à une table |
| `@Table(name = "PRODUCTS")` | nom de la table |
| `@Id` | clé primaire |
| `@GeneratedValue` | l'id est généré automatiquement |
| `@Getter` / `@Setter` (Lombok) | génère les getters et setters |
| `@NoArgsConstructor` / `@AllArgsConstructor` (Lombok) | constructeur sans paramètres (obligatoire pour JPA) / avec tous les paramètres |
| `@ToString` (Lombok) | génère `toString()` |
| `@Builder` (Lombok) | permet `Product.builder().name(...).build()` |

Table créée par Hibernate :

```sql
create table products (price float(53) not null, quantity integer not null,
                       id bigint not null, name varchar(255), primary key (id))
```

## 3. `application.properties`

```properties
spring.application.name=demo-bdcc-spring-data
spring.datasource.url=jdbc:h2:mem:products-db
spring.h2.console.enabled=true
server.port=8084
```

- `jdbc:h2:mem:products-db` : base H2 **en mémoire** nommée `products-db` (les données disparaissent à l'arrêt).
- `spring.h2.console.enabled=true` : active la console web H2 sur `/h2-console`.
- `server.port=8084` : port du serveur Tomcat embarqué.

![Console H2](captures/h2-console.png)

## 4. `ProductRepository`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContains(String keyword);
}
```

- `JpaRepository<Product, Long>` : l'entité gérée et le type de son id. On hérite sans rien implémenter de `save`, `findAll`, `findById`, `deleteById`, `count`, etc.
- `findByNameContains` : Spring Data analyse le nom de la méthode : `find` = SELECT, `By` = WHERE, `Name` = l'attribut `name`, `Contains` = `LIKE %keyword%`.

SQL généré :

```sql
select ... from products p1_0 where p1_0.name like ? escape '\'
```

## 5. Tests des opérations (CommandLineRunner)

Un `@Bean CommandLineRunner` s'exécute au démarrage et teste chaque opération. Les produits sont créés de trois façons : constructeur sans paramètres + setters, constructeur avec paramètres, Builder.

| Opération | Code | SQL exécuté |
|---|---|---|
| Ajouter | `productRepository.save(p)` | `insert into products ...` |
| Consulter tous | `productRepository.findAll()` | `select ... from products` |
| Consulter un | `productRepository.findById(1L)` | `select ... where id=?` |
| Chercher | `productRepository.findByNameContains("C")` | `select ... where name like ?` |
| Mettre à jour | modifier l'objet puis `save(product)` | `update products set ... where id=?` |
| Supprimer | `productRepository.deleteById(3L)` | `delete from products where id=?` |

Sortie console :

```
Product(id=1, name=Computer, price=3400.0, quantity=2)
Product(id=2, name=Printer, price=1200.0, quantity=13)
Product(id=3, name=Smart phone, price=1300.0, quantity=4)
Produit 1 : Product(id=1, name=Computer, price=3400.0, quantity=2)
Product(id=1, name=Computer, price=3400.0, quantity=2)
```

Pour la mise à jour, `save` reçoit un objet dont l'id existe déjà : JPA fait un `UPDATE` au lieu d'un `INSERT`.

![Sortie console](captures/console.png)

## 6. Bonus : web service REST et Swagger

`ProductRestController` (`@RestController`, préfixe `@RequestMapping("/api")`) :

| Méthode | URL | Action |
|---|---|---|
| GET | `/api/products` | liste des produits |
| GET | `/api/products/{id}` | un produit (exception `Product not found` si absent) |
| GET | `/api/find/products?kw=...` | recherche par mot-clé dans le nom |
| POST | `/api/products` | ajoute un produit (corps JSON, sans id) |
| DELETE | `/api/products/{id}` | supprime un produit |

Exemple de POST :

```json
{"name": "HP", "price": 3000, "quantity": 3}
```

La documentation est générée par **springdoc-openapi** : Swagger UI sur `/swagger-ui.html`, description OpenAPI sur `/v3/api-docs` (importable dans Postman).

![Swagger UI](captures/swagger-ui.png)
