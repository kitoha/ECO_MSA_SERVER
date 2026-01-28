import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // Fetch all products
    const response = http.get(`${BASE_URL}/api/v1/products?page=0&size=100`);
    
    check(response, {
        'status is 200': (r) => r.status === 200,
    });
    
    if (response.status === 200) {
        const products = JSON.parse(response.body);
        const productIds = products.map(p => p.id);
        
        console.log('\n=== Product IDs ===');
        console.log(JSON.stringify(productIds, null, 2));
        console.log(`\nTotal products: ${productIds.length}`);
    }
}
