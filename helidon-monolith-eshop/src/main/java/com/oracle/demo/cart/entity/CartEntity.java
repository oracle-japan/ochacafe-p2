package com.oracle.demo.cart.entity;

import java.util.Collection;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * ショッピングカートを示すエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "CartUnit")
@Entity(name = "CartEntity")
@Table(name = "CART_TABLE")
public class CartEntity {

	@Id
	@Column(
			insertable = true,
			name = "ID",
			nullable = false,
			updatable = false
		)
	private String id;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "USER_ID",
			nullable = false,
			updatable = false
		)
	private String userId;

	@OneToMany(
			mappedBy = "cart",
			cascade = {CascadeType.ALL},
			orphanRemoval = true
		)
	private Collection<CartItemEntity> items;


	@Deprecated
	protected CartEntity() {
	}

	private CartEntity(String userId) {
		this.userId = userId;
		this.id = userId;
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
