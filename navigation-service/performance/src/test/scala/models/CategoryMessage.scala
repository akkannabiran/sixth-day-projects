package models

import net.liftweb.json._

case class CategoryMessage(
  eventType: String = "CATEGORY_UPDATED",
  messageType: String = "Category Message",
  id: String = "DEFAULT_PRODUCT_ID",
  displayName: String = "DEFAULT_NAME",
  name: String = "DEFAULT_NAME",
  leftNavImageAvailableOverride: String = "leftNavImageAvailableOverride",

  //SEO Fields
  alternateSeoName: String = "DEFAULT_ALTERNATE_SEO_NAME",
  seoTitleOverride: String = "DEFAULT_seoTitleOverride",
  canonicalUrl: String = "canonicalUrl",
  seoContentTitle: String = "seoContentTitle",
  seoContentDescription: String = "seoContentDescription",
  seoTags: String = "seoTags",

  //Flags
  boutique: Boolean = false,
  imageAvailable: Boolean = false,
  htmlAvailable: Boolean = false,
  expandCategory: Boolean = false,
  dontShowChildren: Boolean = false,
  personalized: Boolean = false,
  hidden: Boolean = false,
  noResults: Boolean = false,
  displayAsGroups: Boolean = false,
  driveToGroupPDP: Boolean = false,
  excludeFromPCS: Boolean = false
) {

  def toJson = {
    Serialization.write (this) (DefaultFormats)
  }
}
