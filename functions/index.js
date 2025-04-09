const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.updateOrderStatus = functions.pubsub
    .schedule("every 5 minutes")
    .onRun(async (context) => {
      const now = Date.now();
      const ordersRef = admin.database().ref("/Orders");
      try {
        const snapshot = await ordersRef.once("value");
        const orders = snapshot.val();

        for (const userID in orders) {
          if (Object.prototype.hasOwnProperty.call(orders, userID)) {
            const userOrders = orders[userID];
            for (const order of userOrders) {
              const orderID = Object.keys(order)[0];
              const orderData = order[orderID];
              const cookingEndTime = orderData.cookingEndTime;
              const deliveryEndTime = orderData.deliveryEndTime;
              // Kiểm tra nếu thời gian nấu đã qua và cập nhật trạng thái
              if (now > cookingEndTime && orderData.status !== "Delivering") {
                await ordersRef
                    .child(userID)
                    .child(orderID)
                    .update({status: "Delivering"});
              }

              // Kiểm tra nếu thời gian giao hàng đã qua và cập nhật trạng thái
              if (now > deliveryEndTime && orderData.status !== "Done") {
                await ordersRef
                    .child(userID)
                    .child(orderID)
                    .update({status: "Done"});
              }
            }
          }
        }
      } catch (error) {
        console.error("Error updating order status:", error);
      }
    });
