package com.app.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.app.dto.InventoryResponse;
import com.app.dto.OrderLineItemsDto;
import com.app.dto.OrderRequest;
import com.app.model.Order;
import com.app.model.OrderLineItem;
import com.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	private final OrderRepository orderRepository;
	private final WebClient.Builder webClientBuilder;
	
public void placeOrder(OrderRequest orderRequest) {
	Order order = new Order();
	order.setOrderNumber(UUID.randomUUID().toString());
	List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemDtoList()
	.stream()
	.map(this ::mapToDto).toList();
	order.setOrderLineItem(orderLineItems);
	List<String> skuCodes = order.getOrderLineItem().stream().map(OrderLineItem::getSkuCode).toList();

	//call inventory service, place order if product is in stock
	InventoryResponse[] inventryResponsesArray = webClientBuilder.build().get()
			.uri("http://inventory-service/api/inventory", uriBuilder->uriBuilder.queryParam("skuCode", skuCodes).build())
					.retrieve().bodyToMono(InventoryResponse[].class).block();
	boolean allProductInStock = Arrays.stream(inventryResponsesArray).allMatch(InventoryResponse::isInStock);
	if(allProductInStock) {
		orderRepository.save(order);
	}
	else {
		throw new IllegalStateException("product not in stock, Please try again later");
	}
}
private OrderLineItem mapToDto(OrderLineItemsDto orderLineItemsDto) {
	OrderLineItem orderLineItem = new OrderLineItem();
	orderLineItem.setPrice(orderLineItemsDto.getPrice());
	orderLineItem.setQuantity(orderLineItemsDto.getQuantity());
	orderLineItem.setSkuCode(orderLineItemsDto.getSkuCode());
	return orderLineItem;
}

}
