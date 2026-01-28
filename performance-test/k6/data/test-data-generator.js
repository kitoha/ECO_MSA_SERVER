/**
 * 테스트 데이터 생성 유틸리티
 *
 * 부하 테스트를 위한 초기 데이터 생성
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    console.log('Generating test data...');

    // 카테고리는 Flyway 마이그레이션으로 이미 생성됨
    // ID: 1=전자기기, 2=패션, 3=생활용품, 4=도서
    const categories = [
        { id: 1, name: '전자기기' },
        { id: 2, name: '패션' },
        { id: 3, name: '생활용품' },
        { id: 4, name: '도서' }
    ];
    console.log('Using existing categories from Flyway migration');

    // 1. 상품 생성
    console.log('Creating products...');
    createProducts(categories, 100);

    // 2. 재고 설정 (Inventory Service가 있다면)
    // console.log('Setting up inventory...');
    // setupInventory(100);

    console.log('✅ Test data generation completed!');
}

function createProducts(categories, count) {
    const productNames = [
        'Laptop', 'Smartphone', 'Tablet', 'Monitor', 'Keyboard',
        'T-Shirt', 'Jeans', 'Jacket', 'Shoes', 'Hat',
        'Novel', 'Textbook', 'Magazine', 'Comic', 'Dictionary',
        'Chair', 'Table', 'Lamp', 'Rug', 'Plant',
        'Basketball', 'Tennis Racket', 'Yoga Mat', 'Dumbbell', 'Bicycle'
    ];

    // Product Service는 배치 생성을 지원하므로, 10개씩 묶어서 생성
    const batchSize = 10;

    for (let batch = 0; batch < Math.ceil(count / batchSize); batch++) {
        const products = [];
        const startIdx = batch * batchSize + 1;
        const endIdx = Math.min((batch + 1) * batchSize, count);

        for (let i = startIdx; i <= endIdx; i++) {
            const categoryId = categories[i % categories.length].id;
            const name = `${productNames[i % productNames.length]} ${i}`;
            const originalPrice = Math.floor(Math.random() * 90000) + 10000; // 10,000 ~ 100,000 KRW
            const salePrice = Math.floor(originalPrice * (0.7 + Math.random() * 0.3)); // 70~100% of original

            products.push({
                name: name,
                description: `High quality ${name}`,
                categoryId: categoryId,
                originalPrice: originalPrice,
                salePrice: salePrice,
                status: 'ACTIVE',
                images: []
            });
        }

        // 배치로 상품 생성
        const response = http.post(
            `${BASE_URL}/api/v1/products`,
            JSON.stringify(products),
            {
                headers: { 'Content-Type': 'application/json' },
            }
        );

        if (check(response, { 'products created': (r) => r.status === 200 || r.status === 201 })) {
            console.log(`  ✓ Created ${endIdx} products...`);
        } else {
            console.log(`  ✗ Failed to create batch ${batch + 1}: ${response.status} - ${response.body}`);
        }

        sleep(0.2);
    }
}
