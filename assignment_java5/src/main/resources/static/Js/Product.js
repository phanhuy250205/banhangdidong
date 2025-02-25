
//Thêm sản phẩm bên prodct
function addToCart(button) {
    let form = button.closest("form");
    let sanPhamId = form.querySelector("input[name='sanPhamId']").value;

    fetch('/cart/add?sanPhamId=' + sanPhamId, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Lỗi khi gửi request!");
            }
            return response.json();
        })
        .then(data => {
            console.log("Phản hồi từ server:", data);

            if (data.status === "success") {
                let cartBadge = document.getElementById('cart-badge');
                if (cartBadge) {
                    cartBadge.textContent = data.cartCount; // ✅ Cập nhật theo số lượng thực tế
                }
                alert("✅ Đã thêm vào giỏ hàng!");
            } else {
                alert("❌ Lỗi: " + data.message);
            }
        })
        .catch(error => {
            console.error('Lỗi:', error);
            alert("❌ Đã xảy ra lỗi khi thêm vào giỏ!");
        });
}

