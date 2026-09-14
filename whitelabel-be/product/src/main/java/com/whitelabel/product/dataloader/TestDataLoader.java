package com.whitelabel.product.dataloader;

import com.whitelabel.product.model.Price;
import com.whitelabel.product.model.Product;
import com.whitelabel.product.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {
    private final IProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {

        Product product = Product.builder()
                .title("Iphone 17")
                .brand("apple")
                .image("https://google/com.br")
                .gallery(List.of("https://google/com.br", "https://google/com.br"))
                .description("Lorem Ipsum Dolor Sit")
                .status("IN")
                .price(Price.builder()
                        .originalPrice(BigDecimal.valueOf(1000))
                        .discountPrice(BigDecimal.valueOf(800))
                        .discountPercentage(20)
                        .build()
                )
                .build();

        productRepository.save(product);

        System.out.println("Test data loaded successfully.");

    }
}
