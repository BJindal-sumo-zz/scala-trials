import scala.collection.mutable.HashMap
class myAp{
  private var internsHmap : HashMap[String, String] = HashMap("bjindal"->"Bharat Jindal","ssarkar"->"Siddhant Sarkar")
  def get(Key : String) : String = {
    internsHmap(Key)
  }
  def put(Key : String , KeyValue : String) {
    internsHmap += (Key -> KeyValue)
    println("Succeded")
  }
  def insertIfNotPresent(Key : String , KeyValue : String) {
    if (internsHmap.contains(Key))
      println("Present")
    else {
      internsHmap += (Key -> KeyValue)
      println("Inserted")
    }
  }
  def update(Key : String , KeyValue : String) {
    if (internsHmap.contains(Key)) {
      internsHmap(Key) = KeyValue
      println(s"value changed to ${internsHmap(Key)}")
    }
    else {
      println("Not present")
    }
  }
  def delete(Key : String): Unit ={
    if (internsHmap.contains(Key)) {
      internsHmap -= Key
      println(s"$Key removed from map")
    }
    else
      println("Not present")
  }
}
val st = new myAp
st get "bjindal"
st put ("atiwari","Aman Tiwari")
st update ("bjindal","BJIndal")
st insertIfNotPresent ("atiwari","Aman Tiwari")
st insertIfNotPresent ("akarim","Asad Karim")
st delete "ssarkar"
st get "atiwari"
st get "ssarkar"