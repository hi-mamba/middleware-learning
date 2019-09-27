package space.pankui.exmaple.register;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pankui
 * @date 2019/9/27
 * <pre>
 *
 * </pre>
 */
@RequestMapping("/product")
@RestController
public class ProductController {

    @RequestMapping("/get/{id}")
    public Object getProduct(HttpServletRequest request, @PathVariable("id") String id) {
        int localPort = request.getLocalPort();
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("productName", localPort);
        return map;
    }
}
