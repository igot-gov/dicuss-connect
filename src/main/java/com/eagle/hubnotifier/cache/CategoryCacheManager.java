package com.eagle.hubnotifier.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eagle.hubnotifier.constants.Constants;
import com.eagle.hubnotifier.model.HubCategory;
import com.eagle.hubnotifier.repository.HubCategoryRepository;

/**
 * Cache Manager to hold the Category Details
 * 
 * @author karthikeyan.rajendran@tarento.com
 *
 */
@Component
public class CategoryCacheManager {

	// Singleton Instance
	private static volatile CategoryCacheManager cacheManager;

	// Map to store the Category details
	private Map<Integer, String> categoryMap = null;

	// Category Repository to get the Category Details
	@Autowired
	private HubCategoryRepository categoryRepository;

	/**
	 * Private constructor to avoid duplicate object creation
	 */
	private CategoryCacheManager() {
		categoryMap = new HashMap<>(5);
	}

	/**
	 * Returns the singleton object for this class
	 * 
	 * @return - Returns CategoryCacheManager instance
	 */
	public static CategoryCacheManager getInstance() {
		if (cacheManager == null) {
			synchronized (CategoryCacheManager.class) {
				if (cacheManager == null) {
					cacheManager = new CategoryCacheManager();
				}
			}
		}
		return cacheManager;
	}

	public String getCategoryName(int categoryId) {
		String categoryName = "";

		if (categoryMap.containsKey(categoryId)) {
			categoryName = categoryMap.get(categoryId);
		} else {
			HubCategory category = categoryRepository.findByKey(Constants.CATEGORY_VALUE_CONSTANTS + ":" + categoryId);
			if (category != null) {
				categoryName = category.getName();
				categoryMap.put(categoryId, categoryName);
			}
		}

		return categoryName;
	}
}
