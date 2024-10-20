package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import ecommerce.configuration.auth.AuthRoles;
import ecommerce.dto.addresses.InAddress;
import ecommerce.dto.orders.InOrder;
import ecommerce.dto.orders.InOrderCompletedAtUpdate;
import ecommerce.dto.orders.InOrderFilters;
import ecommerce.dto.orders.InOrderProduct;
import ecommerce.dto.orders.OutOrder;
import ecommerce.dto.payments.InPayment;
import ecommerce.dto.payments.InPaymentCompletedAtUpdate;
import ecommerce.dto.shared.InPagination;
import ecommerce.exception.ConflictException;
import ecommerce.exception.NotFoundException;
import ecommerce.exception.ValidationException;
import ecommerce.repository.addresses.AddressesRepository;
import ecommerce.repository.addresses.entity.Address;
import ecommerce.repository.categories.entity.Category;
import ecommerce.repository.countries.entity.Country;
import ecommerce.repository.orders.OrderProductsRepository;
import ecommerce.repository.orders.OrdersRepository;
import ecommerce.repository.orders.entity.Order;
import ecommerce.repository.orders.entity.OrderProduct;
import ecommerce.repository.paymentmethods.entity.PaymentMethod;
import ecommerce.repository.payments.PaymentsRepository;
import ecommerce.repository.payments.entity.Payment;
import ecommerce.repository.products.ProductsRepository;
import ecommerce.repository.products.entity.Product;
import ecommerce.service.addresses.mapper.AddressesMapper;
import ecommerce.service.countries.CountriesService;
import ecommerce.service.orders.OrdersService;
import ecommerce.service.orders.mapper.OrderProductsMapper;
import ecommerce.service.orders.mapper.OrdersMapper;
import ecommerce.service.orders.mapper.OrdersSpecificationMapper;
import ecommerce.service.paymentmethods.PaymentMethodsService;
import ecommerce.service.payments.mapper.PaymentsMapper;
import ecommerce.service.products.mapper.ProductsMapper;
import ecommerce.service.utils.mapper.PaginationMapper;
import ecommerce.service.utils.sanitizer.IUserInputSanitizer;

public class OrdersServiceTest {

    private CountriesService countriesService;
    private PaymentMethodsService paymentMethodsService;
    private OrdersMapper ordersMapper;
    private OrderProductsMapper orderProductsMapper;
    private AddressesMapper addressesMapper;
    private PaymentsMapper paymentsMapper;
    private PaginationMapper paginationMapper;
    private OrdersSpecificationMapper ordersSpecificationMapper;
    private OrdersRepository ordersRepository;
    private OrderProductsRepository orderProductsRepository;
    private ProductsRepository productsRepository;
    private AddressesRepository addressesRepository;
    private PaymentsRepository paymentsRepository;

    // indirect dependencies
    private ProductsMapper productsMapper;
    private IUserInputSanitizer userInputSanitizer;

    @BeforeEach
    public void setupDependencies() throws ValidationException {
        // indirect dependencies
        userInputSanitizer = Mockito.mock(IUserInputSanitizer.class);
        Mockito
            .when(userInputSanitizer.sanitize(Mockito.anyString()))
            .then(AdditionalAnswers.returnsFirstArg());
        productsMapper = new ProductsMapper(userInputSanitizer);

        countriesService = Mockito.mock(CountriesService.class);
        paymentMethodsService = Mockito.mock(PaymentMethodsService.class);
        addressesMapper = new AddressesMapper();
        paymentsMapper = new PaymentsMapper();
        orderProductsMapper = new OrderProductsMapper(productsMapper);
        ordersMapper = new OrdersMapper(orderProductsMapper, addressesMapper, paymentsMapper);
        paginationMapper = new PaginationMapper();
        ordersSpecificationMapper = new OrdersSpecificationMapper();
        ordersRepository = Mockito.mock(OrdersRepository.class);
        orderProductsRepository = Mockito.mock(OrderProductsRepository.class);
        productsRepository = Mockito.mock(ProductsRepository.class);
        addressesRepository = Mockito.mock(AddressesRepository.class);
        paymentsRepository = Mockito.mock(PaymentsRepository.class);
    }

    private OrdersService createService() {
        return new OrdersService(
            countriesService, 
            paymentMethodsService, 
            ordersMapper, 
            orderProductsMapper, 
            addressesMapper, 
            paymentsMapper, 
            paginationMapper, 
            ordersSpecificationMapper, 
            ordersRepository, 
            orderProductsRepository, 
            productsRepository, 
            addressesRepository, 
            paymentsRepository
        );
    }

    private Authentication createUser(String... roles) {
        final var authorities = Arrays.asList(roles)
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        final var userDetails = new User("test user", "dummy password", authorities);
        final var authentication = new TestingAuthenticationToken(userDetails, null, roles);

        return authentication;
    }

    private Order createOrder(long id, String username) {
        final var country = Country.builder()
            .id(1L)
            .active(true)
            .name("name")
            .build();
        final var address = Address.builder()
            .id(id)
            .street("street")
            .house("house")
            .postalCode("postalCode")
            .city("city")
            .country(country)
            .order(null)
            .build();
        final var paymentMethod = PaymentMethod.builder()
            .id(1L)
            .active(true)
            .name("name")
            .description("description")
            .build();
        final var payment = Payment.builder()
            .id(id)
            .paymentMethod(paymentMethod)
            .amount(BigDecimal.valueOf(10.00))
            .completedAt(null)
            .order(null)
            .build();
        final var category = Category.builder()
            .id(1L)
            .name("name")
            .parentCategory(null)
            .childCategories(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(1L)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(1.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));
        final var order = Order.builder()
            .id(id)
            .username(username)
            .address(address)
            .payment(payment)
            .orderedAt(LocalDateTime.now())
            .completedAt(null)
            .orderProducts(Collections.emptyList())
            .build();
        final var orderProducts = List.of(
            OrderProduct.builder()
                // .id(1L)
                .product(product)
                .order(order)
                .price(product.getPrice())
                .quantity(2)
                .build()
        );
        order.setOrderProducts(orderProducts);
        address.setOrder(order);
        payment.setOrder(order);

        return order;
    }

    private void assertCorrectEntitiyToOutMapping(Order order, OutOrder out) {
        assertEquals(order.getId(), out.id());
        assertEquals(order.getUsername(), out.username());
        assertEquals(order.getAddress().getId(), out.address().id());
        assertEquals(order.getAddress().getStreet(), out.address().street());
        assertEquals(order.getAddress().getHouse(), out.address().house());
        assertEquals(order.getAddress().getPostalCode(), out.address().postalCode());
        assertEquals(order.getAddress().getCity(), out.address().city());
        assertEquals(order.getAddress().getCountry().getId(), out.address().country());
        assertEquals(order.getOrderedAt(), out.orderedAt());
        assertEquals(order.getCompletedAt(), out.completedAt());
        assertEquals(order.getPayment().getId(), out.payment().id());
        assertEquals(order.getPayment().getPaymentMethod().getId(), out.payment().paymentMethod());
        assertEquals(order.getPayment().getAmount(), out.payment().amount());
        assertEquals(order.getOrderProducts().size(), out.orderProducts().size());
        for (int i = 0; i < order.getOrderProducts().size(); ++i) {
            final var orderProduct = order.getOrderProducts().get(i);
            final var outOrderProduct = out.orderProducts().get(i);
            assertEquals(orderProduct.getProduct().getId(), outOrderProduct.product().id());
            assertEquals(orderProduct.getProduct().getName(), outOrderProduct.product().name());
            assertEquals(orderProduct.getProduct().getPrice(), outOrderProduct.product().price());
            assertEquals(orderProduct.getProduct().getCategory().getId(), outOrderProduct.product().category());
            assertEquals(orderProduct.getQuantity(), outOrderProduct.quantity());
        }
    }

    //#region getOrder

    @Test
    public void getOrder_notFound() {
        final Long id = 1L;
        final var user = createUser();
        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.anyLong(), Mockito.anyString());

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.getOrder(user, id);
        });
        Mockito
            .verify(ordersRepository, Mockito.never())
            .findById(Mockito.anyLong());
    }

    private void test_getOrder_notFound_privileged(String role) {
        final Long id = 1L;
        final var user = createUser(role);
        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findById(Mockito.anyLong());

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.getOrder(user, id);
        });
        Mockito
            .verify(ordersRepository, Mockito.never())
            .findByIdAndUsername(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    public void getOrder_notFound_privileged_SEARCH() {
        test_getOrder_notFound_privileged(AuthRoles.ORDER_SEARCH);
    }

    @Test
    public void getOrder_notFound_privileged_UPDATE() {
        test_getOrder_notFound_privileged(AuthRoles.ORDER_UPDATE);
    }

    private void test_getOrder(String... roles) throws NotFoundException {
        final var user = createUser(roles);
        final var order = createOrder(1L, user.getName());

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(order.getId()));
        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.eq(order.getId()), Mockito.anyString());

        final var service = createService();

        final var out = service.getOrder(user, order.getId());

        assertCorrectEntitiyToOutMapping(order, out);
    }

    @Test
    public void getOrder() throws NotFoundException {
        test_getOrder();

        Mockito
            .verify(ordersRepository, Mockito.never())
            .findById(Mockito.anyLong());
    }

    @Test
    public void getOrder_privileged_privileged_SEARCH() throws NotFoundException {
        test_getOrder(AuthRoles.ORDER_SEARCH);

        Mockito
            .verify(ordersRepository, Mockito.never())
            .findByIdAndUsername(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    public void getOrder_privileged_privileged_UPDATE() throws NotFoundException {
        test_getOrder(AuthRoles.ORDER_UPDATE);

        Mockito
            .verify(ordersRepository, Mockito.never())
            .findByIdAndUsername(Mockito.anyLong(), Mockito.anyString());
    }
    
    //#endregion

    //#region getOrders

    @Test
    public void getOrders() {
        final var user = createUser();
        final var orders = List.of(
            createOrder(1L, user.getName()),
            createOrder(2L, user.getName()),
            createOrder(3L, user.getName())
        );
        final var filters = new InOrderFilters(null, null);
        final var pagination = new InPagination(5, 1);
        
        final var ordersPage = new PageImpl<Order>(
            orders,
            PageRequest.of(pagination.pageIdx(), pagination.pageSize()),
            orders.size()
        );

        Mockito
            .doReturn(ordersPage)
            .when(ordersRepository)
            .findAll(Mockito.<Specification<Order>>any(), Mockito.<Pageable>any());

        final var service = createService();

        final var outPage = service.getOrders(user, filters, pagination);
        assertEquals(pagination.pageIdx(), outPage.pageIdx());
        assertEquals(pagination.pageSize(), outPage.pageSize());
        assertEquals(
            pagination.pageSize() * pagination.pageIdx() + orders.size(),
            outPage.totalElements()
        );
        assertEquals(orders.size(), outPage.content().size());
        assertEquals(
            pagination.pageIdx() + 1,
            outPage.totalPages()
        );
        assertEquals(orders.size(), outPage.content().size());
        for (int i = 0; i < orders.size(); ++i) {
            final var order = orders.get(i);
            final var outOrder = outPage.content().get(i);
            assertCorrectEntitiyToOutMapping(order, outOrder);
        }
    }
    
    // TODO: write tests that check if underprivileged user adds his username to specification

    // TODO: write tests that check specification overall

    //#endregion

    //#region postOrder
    
    @Test
    public void postOrder_duplicatedProduct() {
        final var user = createUser();
        final var inOrder = new InOrder(
            new InAddress(
                "street",
                "house",
                "postalCode",
                "city",
                1L
            ),
            new InPayment(1L),
            List.of(
                new InOrderProduct(1L, 10),
                new InOrderProduct(1L, 15)
            )
        );

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.postOrder(user, inOrder);
        });
    }

    @Test
    public void postOrder_countryNotFound() throws NotFoundException {
        final Long countryId = 1L;
        final var user = createUser();
        final var inOrder = new InOrder(
            new InAddress(
                "street",
                "house",
                "postalCode",
                "city",
                countryId
            ),
            new InPayment(1L),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );

        Mockito
            .doThrow(NotFoundException.class)
            .when(countriesService)
            .findByIdActive(Mockito.eq(countryId));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.postOrder(user, inOrder);
        });
    }

    @Test
    public void postOrder_paymentMethodNotFound() throws NotFoundException {
        final Long countryId = 1L;
        final Long paymentMethodId = 1L;
        final var user = createUser();
        final var inOrder = new InOrder(
            new InAddress(
                "street",
                "house",
                "postalCode",
                "city",
                countryId
            ),
            new InPayment(paymentMethodId),
            List.of(
                new InOrderProduct(1L, 10)
            )
        );
        final var country = Country.builder()
            .id(countryId)
            .active(true)
            .name("name")
            .build();

        Mockito
            .doReturn(country)
            .when(countriesService)
            .findByIdActive(Mockito.eq(countryId));
        Mockito
            .doThrow(NotFoundException.class)
            .when(paymentMethodsService)
            .findByIdActive(Mockito.eq(paymentMethodId));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.postOrder(user, inOrder);
        });
    }

    @Test
    public void postOrder_notAllProductsFound() throws NotFoundException {
        final Long countryId = 1L;
        final Long paymentMethodId = 1L;
        final var user = createUser();
        final var inOrder = new InOrder(
            new InAddress(
                "street",
                "house",
                "postalCode",
                "city",
                countryId
            ),
            new InPayment(paymentMethodId),
            List.of(
                new InOrderProduct(1L, 10),
                new InOrderProduct(2L, 15)
            )
        );
        final var country = Country.builder()
            .id(countryId)
            .active(true)
            .name("name")
            .build();
        final var paymentMethod = PaymentMethod.builder()
            .id(paymentMethodId)
            .active(true)
            .name("name")
            .description("description")
            .build();
        final var category = Category.builder()
            .id(1L)
            .name("name")
            .parentCategory(null)
            .products(Collections.emptyList())
            .build();
        final var product = Product.builder()
            .id(1L)
            .active(true)
            .name("name")
            .description("description")
            .price(BigDecimal.valueOf(1.00))
            .category(category)
            .build();
        category.setProducts(List.of(product));

        Mockito
            .doReturn(country)
            .when(countriesService)
            .findByIdActive(Mockito.eq(countryId));
        Mockito
            .doReturn(paymentMethod)
            .when(paymentMethodsService)
            .findByIdActive(Mockito.eq(paymentMethodId));
        Mockito
            .doReturn(List.of(product))
            .when(productsRepository)
            .findByActiveTrueAndIdIn(Mockito.any());

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.postOrder(user, inOrder);
        });
    }

    @Test
    public void postOrder() throws NotFoundException, ValidationException {
        final Long countryId = 1L;
        final Long paymentMethodId = 1L;
        final var user = createUser();
        final var inOrder = new InOrder(
            new InAddress(
                "street",
                "house",
                "postalCode",
                "city",
                countryId
            ),
            new InPayment(paymentMethodId),
            List.of(
                new InOrderProduct(1L, 10),
                new InOrderProduct(2L, 15)
            )
        );
        final var country = Country.builder()
            .id(countryId)
            .active(true)
            .name("name")
            .build();
        final var paymentMethod = PaymentMethod.builder()
            .id(paymentMethodId)
            .active(true)
            .name("name")
            .description("description")
            .build();
        final var category = Category.builder()
            .id(1L)
            .name("name")
            .parentCategory(null)
            .products(Collections.emptyList())
            .build();
        final var products = List.of(
            Product.builder()
                .id(1L)
                .active(true)
                .name("name")
                .description("description")
                .price(BigDecimal.valueOf(1.00))
                .category(category)
                .build(),
            Product.builder()
                .id(2L)
                .active(true)
                .name("name")
                .description("description")
                .price(BigDecimal.valueOf(2.00))
                .category(category)
                .build()
        );
        category.setProducts(products);

        // Wrapper that allows to assign value from lambda expression
        final var savedOrder = new ArrayList<Order>(1);

        Mockito
            .doReturn(country)
            .when(countriesService)
            .findByIdActive(Mockito.eq(countryId));
        Mockito
            .doReturn(paymentMethod)
            .when(paymentMethodsService)
            .findByIdActive(Mockito.eq(paymentMethodId));
        Mockito
            .doReturn(products)
            .when(productsRepository)
            .findByActiveTrueAndIdIn(Mockito.any());
        Mockito
            .when(ordersRepository.save(Mockito.any()))
            .then((invocation) -> {
                final var saved = invocation.getArgument(0, Order.class);
                saved.setId(1L);
                saved.getAddress().setId(1L);
                saved.getPayment().setId(1L);

                // save order as 'savedOrder' to be able to assert mapping
                savedOrder.add(saved);

                return saved;
            });
        Mockito
            .when(orderProductsRepository.saveAll(Mockito.any()))
            .then((invocation) -> {
                final var saved = invocation.getArgument(0, List.class);
                for (int i = 0; i < saved.size(); ++i) {
                    final var orderProduct = (OrderProduct) saved.get(i);
                    orderProduct.setId(i + 1L);
                }
                return saved;
            });

        final var service = createService();

        final var timeBeg = LocalDateTime.now();
        final var out = service.postOrder(user, inOrder);
        final var timeEnd = LocalDateTime.now();

        assertCorrectEntitiyToOutMapping(savedOrder.get(0), out);
        Mockito
            .verify(orderProductsRepository)
            .saveAll(
                Mockito.assertArg((savedIterable) -> {
                    final var saved = new ArrayList<>(IterableUtil.toCollection(savedIterable));
                    assertEquals(inOrder.products().size(), saved.size());

                    saved.sort((a, b) -> Long.compare(a.getId(), b.getId()));
                    final var sortedInOrderProducts = inOrder.products()
                        .stream()
                        .sorted((a, b) -> Long.compare(a.productId(), b.productId()))
                        .collect(Collectors.toList());

                    for (int i = 0; i < saved.size(); ++i) {
                        final var savedOrderProduct = saved.get(i);
                        final var inOrderProduct = sortedInOrderProducts.get(i);
                        assertEquals(inOrderProduct.productId(), savedOrderProduct.getProduct().getId());
                        assertEquals(inOrderProduct.quantity(), savedOrderProduct.getQuantity());
                        assertEquals(savedOrderProduct.getPrice(), savedOrderProduct.getProduct().getPrice());
                    }
                })
            );
        Mockito
            .verify(ordersRepository)
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(user.getName(), saved.getUsername());
                    assertEquals(inOrder.address().street(), saved.getAddress().getStreet());
                    assertEquals(inOrder.address().house(), saved.getAddress().getHouse());
                    assertEquals(inOrder.address().postalCode(), saved.getAddress().getPostalCode());
                    assertEquals(inOrder.address().city(), saved.getAddress().getCity());
                    assertEquals(inOrder.address().country(), saved.getAddress().getCountry().getId());
                    assertEquals(inOrder.payment().paymentMethod(), saved.getPayment().getPaymentMethod().getId());
                    assertEquals(BigDecimal.valueOf(40.00), saved.getPayment().getAmount());
                    assertNull(saved.getPayment().getCompletedAt());
                    final var orderedAt = saved.getOrderedAt();
                    assertTrue(!orderedAt.isBefore(timeBeg) && !orderedAt.isAfter(timeEnd));
                    assertNull(saved.getCompletedAt());
                })
            );
    }
    
    //#endregion

    //#region putOrderAddress

    private void test_putOrderAddress_orderNotFound(
        String... roles
    ) {
        final Long id = 1L;
        final var user = createUser(roles);

        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.eq(id), Mockito.anyString());

        final var inAddress = new InAddress(
            "street",
            "house",
            "postalCode",
            "city",
            1L
        );

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putOrderAddress(user, id, inAddress);
        });
    }

    @Test
    public void putOrderAddress_orderNotFound() {
        test_putOrderAddress_orderNotFound();

        Mockito
            .verify(ordersRepository, Mockito.never())
            .findById(Mockito.anyLong());
    }

    @Test
    public void putOrderAddress_orderNotFound_privileged() {
        test_putOrderAddress_orderNotFound(AuthRoles.ORDER_UPDATE);

        Mockito
            .verify(ordersRepository, Mockito.never())
            .findByIdAndUsername(Mockito.anyLong(), Mockito.anyString());
    }

    private void test_putOrderAddress_orderCompleted(String... roles) {
        final Long id = 1L;
        final var user = createUser(roles);
        final var order = createOrder(id, user.getName());
        order.setCompletedAt(LocalDateTime.now());
        final var inAddress = new InAddress(
            "street",
            "house",
            "postalCode",
            "city",
            15L
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.eq(id), Mockito.eq(user.getName()));        

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.putOrderAddress(user, id, inAddress);
        });
    }
    
    @Test
    public void putOrderAddress_orderCompleted() throws NotFoundException, ConflictException {
        test_putOrderAddress_orderCompleted();
    }

    @Test
    public void putOrderAddress_orderCompleted_privileged() throws NotFoundException, ConflictException {
        test_putOrderAddress_orderCompleted(AuthRoles.ORDER_UPDATE);
    }

    private void test_putOrderAddress_countryNotFound(String... roles) throws NotFoundException {
        final Long id = 1L;
        final var user = createUser(roles);
        final var order = createOrder(id, user.getName());
        final var inAddress = new InAddress(
            "street",
            "house",
            "postalCode",
            "city",
            15L
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.eq(id), Mockito.eq(user.getName()));
        Mockito
            .doThrow(NotFoundException.class)
            .when(countriesService)
            .findByIdActive(Mockito.anyLong());

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putOrderAddress(user, id, inAddress);
        });
    }

    @Test
    public void putOrderAddress_countryNotFound() throws NotFoundException {
        test_putOrderAddress_countryNotFound();
    }

    @Test
    public void putOrderAddress_countryNotFound_privileged() throws NotFoundException {
        test_putOrderAddress_countryNotFound(AuthRoles.ORDER_UPDATE);
    }

    private void test_putOrderAddress(
        String... roles
    ) throws NotFoundException, ConflictException {
        final Long id = 1L;
        final var user = createUser(roles);
        final var order = createOrder(id, user.getName());
        final var inAddress = new InAddress(
            "new street",
            "new house",
            "new postalCode",
            "new city",
            15L
        );
        final var country = Country.builder()
            .id(inAddress.country())
            .active(true)
            .name("name")
            .build();

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));
        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findByIdAndUsername(Mockito.eq(id), Mockito.eq(user.getName()));
        Mockito
            .doReturn(country)
            .when(countriesService)
            .findByIdActive(Mockito.anyLong());

        final var service = createService();

        service.putOrderAddress(user, id, inAddress);

        Mockito
            .verify(addressesRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(order.getAddress().getId(), saved.getId());
                    assertEquals(inAddress.street(), saved.getStreet());
                    assertEquals(inAddress.house(), saved.getHouse());
                    assertEquals(inAddress.postalCode(), saved.getPostalCode());
                    assertEquals(inAddress.city(), saved.getCity());
                    assertEquals(inAddress.country(), saved.getCountry().getId());
                })
            );
    }

    @Test
    public void putOrderAddress() throws NotFoundException, ConflictException {
        test_putOrderAddress();
    }

    @Test
    public void putOrderAddress_privileged() throws NotFoundException, ConflictException {
        test_putOrderAddress(AuthRoles.ORDER_UPDATE);
    }

    //#endregion

    //#region putOrderCompletedAt

    @Test
    public void putOrderCompletedAt_orderNotFound() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(1));
        final var inCompletedAt = new InOrderCompletedAtUpdate(LocalDateTime.now());

        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putOrderCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderCompletedAt_orderAlreadyCompleted() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(1));
        order.setCompletedAt(LocalDateTime.now());
        final var inCompletedAt = new InOrderCompletedAtUpdate(LocalDateTime.now());

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.putOrderCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderCompletedAt_completedAtFromFuture() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(1));
        final var inCompletedAt = new InOrderCompletedAtUpdate(
            LocalDateTime.now().plusDays(1)
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putOrderCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderCompletedAt_completedAtBeforeOrderedAt() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(1));
        final var inCompletedAt = new InOrderCompletedAtUpdate(
            order.getOrderedAt().minusDays(1)
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putOrderCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderCompletedAt() throws NotFoundException, ConflictException, ValidationException {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(1));
        final var inCompletedAt = new InOrderCompletedAtUpdate(
            LocalDateTime.now()
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        service.putOrderCompletedAt(id, inCompletedAt);

        Mockito
            .verify(ordersRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(id, saved.getId());
                    assertEquals(inCompletedAt.completedAt(), saved.getCompletedAt());
                })
            );
    }

    //#endregion

    //#region putOrderPaymentCompletedAt

    @Test
    public void putOrderPaymentCompletedAt_orderNotFound() {
        final Long id = 1L;
        final var inCompletedAt = new InPaymentCompletedAtUpdate(
            LocalDateTime.now()
        );

        Mockito
            .doReturn(Optional.empty())
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(NotFoundException.class, () -> {
            service.putOrderPaymentCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderPaymentCompletedAt_paymentAlreadyCompleted() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(10));
        order.getPayment().setCompletedAt(LocalDateTime.now().minusDays(5));
        final var inCompletedAt = new InPaymentCompletedAtUpdate(
            LocalDateTime.now()
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ConflictException.class, () -> {
            service.putOrderPaymentCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderPaymentCompletedAt_completedAtFromFuture() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(10));
        order.getPayment().setCompletedAt(null);
        final var inCompletedAt = new InPaymentCompletedAtUpdate(
            LocalDateTime.now().plusDays(1)
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putOrderPaymentCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderPaymentCompletedAt_completedAtBeforeOrderedAt() {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(10));
        order.getPayment().setCompletedAt(null);
        final var inCompletedAt = new InPaymentCompletedAtUpdate(
            LocalDateTime.now().minusDays(15)
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        assertThrows(ValidationException.class, () -> {
            service.putOrderPaymentCompletedAt(id, inCompletedAt);
        });
    }

    @Test
    public void putOrderPaymentCompletedAt() throws NotFoundException, ConflictException, ValidationException {
        final Long id = 1L;
        final var order = createOrder(id, "username");
        order.setOrderedAt(LocalDateTime.now().minusDays(10));
        order.getPayment().setCompletedAt(null);
        final var inCompletedAt = new InPaymentCompletedAtUpdate(
            LocalDateTime.now()
        );

        Mockito
            .doReturn(Optional.of(order))
            .when(ordersRepository)
            .findById(Mockito.eq(id));

        final var service = createService();

        service.putOrderPaymentCompletedAt(id, inCompletedAt);

        Mockito
            .verify(paymentsRepository, Mockito.times(1))
            .save(
                Mockito.assertArg((saved) -> {
                    assertEquals(order.getPayment().getId(), saved.getId());
                    assertEquals(inCompletedAt.completedAt(), saved.getCompletedAt());
                })
            );
    }

    //#endregion
}
