package com.organica.services.impl;

import com.organica.entities.Product;
import com.organica.payload.ProductDto;
import com.organica.repositories.ProductRepo;
import com.organica.services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepo productRepo;

    // --------------------------------------------------------------------
    // Create Product
    // --------------------------------------------------------------------
    @Override
    public ProductDto CreateProduct(ProductDto productDto) {

        Product product = modelMapper.map(productDto, Product.class);

        if (product.getImg() != null)
            product.setImg(compressBytes(product.getImg()));

        Product saved = productRepo.save(product);

        ProductDto dto = modelMapper.map(saved, ProductDto.class);
        dto.setImg(null);  // Optional: do not send image to client
        return dto;
    }

    // --------------------------------------------------------------------
    // Read One Product
    // --------------------------------------------------------------------
    @Override
    public ProductDto ReadProduct(Integer productId) {

        Product product = productRepo.findById(productId).orElseThrow();

        ProductDto dto = modelMapper.map(product, ProductDto.class);
        dto.setImg(decompressBytes(product.getImg()));

        return dto;
    }

    // --------------------------------------------------------------------
    // Read All Products
    // --------------------------------------------------------------------
    @Override
    public List<ProductDto> ReadAllProduct() {

        return productRepo.findAll()
                .stream()
                .map(p -> new ProductDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getWeight(),
                        p.getImg() == null ? null : decompressBytes(p.getImg())
                )).collect(Collectors.toList());
    }

    // --------------------------------------------------------------------
    // Delete Product
    // --------------------------------------------------------------------
    @Override
    public void DeleteProduct(Integer productId) {
        productRepo.deleteById(productId);
    }

    // --------------------------------------------------------------------
    // Update Product
    // --------------------------------------------------------------------
    @Override
    public ProductDto UpdateProduct(ProductDto productDto, Integer productId) {

        Product newProduct = productRepo.findById(productId).orElseThrow();

        newProduct.setProductName(productDto.getProductName());
        newProduct.setDescription(productDto.getDescription());
        newProduct.setPrice(productDto.getPrice());
        newProduct.setWeight(productDto.getWeight());

        if (productDto.getImg() != null)
            newProduct.setImg(compressBytes(productDto.getImg()));

        productRepo.save(newProduct);

        ProductDto dto = modelMapper.map(newProduct, ProductDto.class);
        dto.setImg(decompressBytes(newProduct.getImg()));
        return dto;
    }

    // --------------------------------------------------------------------
    // Compress bytes
    // --------------------------------------------------------------------
    public static byte[] compressBytes(byte[] data) {

        if (data == null) return null;

        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        return outputStream.toByteArray();
    }

    // --------------------------------------------------------------------
    // Decompress bytes
    // --------------------------------------------------------------------
    public static byte[] decompressBytes(byte[] data) {

        if (data == null || data.length == 0) return null;

        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        } catch (Exception ignored) {}

        return outputStream.toByteArray();
    }
}
