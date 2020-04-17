package com.oracle.demo.cart.entity;

import java.util.Collection;
import java.util.HashSet;

import javax.json.bind.annotation.JsonbProperty;


/**
 * カート情報のエンティティ
 */
public class CartEntity {

	private String id;
	private String userId;
	private Collection<CartItemEntity> items; //sadd/srem/smembers


	@Deprecated
	protected CartEntity() {
	}

	private CartEntity(String userId) {
		this.userId = userId;
		this.id = userId;
		this.items = new HashSet<CartItemEntity>();
	}

	private CartItemEntity findItem(String productId) {
		CartItemEntity result = null;
		for (CartItemEntity ci : this.items) {
			if (ci.getProductId().equals(productId)) {
				result = ci;
				break;
			}
		}

		return result;
	}

	public static final CartEntity newInstance(String userId) {
		return new CartEntity(userId);
	}

	public void addItem(CartItemEntity item) {
		CartItemEntity target = findItem(item.getProductId());

		if (target != null) {
			target.setCount(target.getCount() + item.getCount());
		} else {
			this.items.add(item);
		}
	}

	public void removeItem(String prodctId, int count) {
		CartItemEntity target = findItem(prodctId);

		if (target != null) {
			if (count > 0) {
				int currentCount = target.getCount() - count;
				target.setCount(currentCount > 0 ? currentCount : 0);
			} else {
				this.items.remove(target);
			}
		}
	}

	public void removeItems(Collection<CartItemEntity> items) {
		for (CartItemEntity item : items) {
			removeItem(item.getProductId(), 0);
		}
	}

	public void removeAll() {
		this.items.clear();
	}

	public String getId() {
		return id;
	}

	@JsonbProperty("user_id")
	public String getUserId() {
		return userId;
	}


	public Collection<CartItemEntity> getItems() {
		return items;
	}

	@JsonbProperty("item_count")
	public int getCount() {
		return items.size();
	}



}
