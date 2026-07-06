import http from 'k6/http';
import { check, sleep } from 'k6';

// Load test for SportBuddy public GET endpoints.
//   k6 run load-test/script.js
//   BASE_URL=http://host:8080 k6 run load-test/script.js
const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    browse: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 10 }, // ramp up to 10 virtual users
        { duration: '20s', target: 10 }, // hold
        { duration: '5s', target: 0 },   // ramp down
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],   // < 1% errors
    http_req_duration: ['p(95)<500'], // 95th percentile under 500ms
  },
};

// Public, unauthenticated pages.
const endpoints = ['/', '/login', '/register'];

export default function () {
  for (const path of endpoints) {
    const res = http.get(`${BASE}${path}`);
    check(res, { 'status is 200': (r) => r.status === 200 });
  }
  sleep(1);
}
