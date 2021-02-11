package requestBuilder

import com.ning.http.client.cookie.Cookie
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder


class RequestBuilder {

  private val baseUrl = "/stores?"

  private val stringBuilder = new StringBuilder()

  private val freeFromAddress: String = "freeFormAddress=${freeFormAddress}&"
  private val brandCode: String = "brandCode=nm&"
  private val mileRadius: String = "mileRadius=${mileRadius}&"
  private val longitudeAndLongitude: String = "latitude=${latitude}&longitude=${longitude}&"
  private val skuId: String = "skuId=${skuId}&"
  private val storeId: String = "storeId=${storeId}&"
  private val quantity: String = "quantity=${quantity}&"

  private var useStubService: Boolean = false;
  private var httpRequestName : String = "Get Stores";

  def builder(): RequestBuilder = {
    this.stringBuilder.append(baseUrl)
    this
  }

  def withFreeFromAddress(): RequestBuilder = {
    this.stringBuilder.append(this.freeFromAddress)
    this
  }

  def withBrandCode(): RequestBuilder = {
    this.stringBuilder.append(this.brandCode)
    this
  }

  def withMileRadius(): RequestBuilder = {
    this.stringBuilder.append(this.mileRadius)
    this
  }

  def withLatitudeAndLongitude(): RequestBuilder = {
    this.stringBuilder.append(this.longitudeAndLongitude)
    this
  }

  def withSkuId(): RequestBuilder = {
    this.stringBuilder.append(this.skuId)
    this
  }

  def withQuantity(): RequestBuilder = {
    this.stringBuilder.append(this.quantity)
    this
  }

  def withStoreId(): RequestBuilder = {
    this.stringBuilder.append(this.storeId)
    this
  }

  def useStubService(useStubService: Boolean): RequestBuilder = {
    this.useStubService = useStubService
    this
  }

  def overrideRequestName(requestName: String): RequestBuilder = {
    this.httpRequestName = requestName
    this
  }

  def build(): HttpRequestBuilder = {
    http(httpRequestName)
      .get(this.stringBuilder.mkString)
      .header(HttpHeaderNames.Cookie, "STUB_GOT_WWW="+this.useStubService)
      .check(status.is(s => 200))
  }
}
