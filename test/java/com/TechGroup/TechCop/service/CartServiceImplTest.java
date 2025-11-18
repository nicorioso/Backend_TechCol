package com.techgroup.techcop.service;

import com.techgroup.techcop.model.CartItem;
import com.techgroup.techcop.model.Carts;
import com.techgroup.techcop.model.Customer;
import com.techgroup.techcop.model.Products;
import com.techgroup.techcop.repository.CartsDBA;
import com.techgroup.techcop.repository.CustomerDBA;
import com.techgroup.techcop.repository.ProductsDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImplTest implements CartServiceTest {

    @Autowired
    private ProductsDBA productsDBA;

    @Autowired
    private CartsDBA cartsDBA;

    @Autowired
    private CustomerDBA customerDBA;

    @Override
    public List<CartItem> getCartItems() {
        List<CartItem> cart = new ArrayList<>();
        List<Carts> carts = cartsDBA.findAll();

        for (Carts c : carts) {
            Products product = productsDBA.findById(c.getProduct_id())
                    .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + c.getProduct_id()));
            Customer customer = customerDBA.findById(c.getCustomer_id())
                    .orElseThrow(() -> new RuntimeException("No existe el cliente con el id: " + c.getCustomer_id()));

            // El cálculo de total ahora lo hace CartItem.getTotal()
            CartItem cartItem = new CartItem(
                    c.getCart_id(),
                    null, // ya no hace falta pasar el total, se calcula con getTotal()
                    c.getAmount(),
                    product,
                    customer
            );
            cart.add(cartItem);
        }
        return cart;
    }

    @Override
    public Carts postCartItem(Carts carts) {
        List<Carts> cartsItems = cartsDBA.findAll();
        for (Carts c : cartsItems) {
            if (c.getProduct_id().equals(carts.getProduct_id()) &&
                    c.getCustomer_id().equals(carts.getCustomer_id())) {

                Products product = productsDBA.findById(carts.getProduct_id())
                        .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + carts.getProduct_id()));

                c.setAmount(c.getAmount() + carts.getAmount());
                c.setCart_price(product.getPrice() * c.getAmount());
                return cartsDBA.save(c);
            }
        }

        Products product = productsDBA.findById(carts.getProduct_id())
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + carts.getProduct_id()));
        carts.setCart_price(product.getPrice() * carts.getAmount());
        return cartsDBA.save(carts);
    }

    @Override
    public void deleteCartItem(Integer cartId) {
        Carts cart = cartsDBA.findById(cartId)
                .orElseThrow(() -> new RuntimeException("No existe el carrito con id: " + cartId));

        if (cart.getAmount() > 1) {
            cart.setAmount(cart.getAmount() - 1);
            Products product = productsDBA.findById(cart.getProduct_id())
                    .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + cart.getProduct_id()));
            cart.setCart_price(product.getPrice() * cart.getAmount());
            cartsDBA.save(cart);
        } else {
            cartsDBA.deleteById(cartId);
        }
    }
}
