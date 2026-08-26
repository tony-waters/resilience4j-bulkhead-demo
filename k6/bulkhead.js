import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export const sentEmails = new Counter('sent_email_responses');
export const bulkheadRejectedEmails = new Counter('bulkhead_rejected_email_responses');
export const unexpectedResponses = new Rate('unexpected_responses');

export const options = {
  scenarios: {
    saturate_email_bulkhead: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '2s',
      preAllocatedVUs: 10,
      maxVUs: 20,
    },
  },
  thresholds: {
    sent_email_responses: ['count>0'],
    bulkhead_rejected_email_responses: ['count>0'],
    unexpected_responses: ['rate<0.01'],
  },
};

export default function () {
  const id = `${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    customerEmail: `bulkhead-${id}@example.com`,
    description: `Bulkhead demo order ${id}`,
    amount: '42.50',
  });

  const response = http.post(`${BASE_URL}/api/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { endpoint: 'POST /api/orders' },
  });

  let body = {};
  if (response.headers['Content-Type'] && response.headers['Content-Type'].includes('application/json')) {
    body = response.json();
  }

  const isSent = response.status === 201 && body.emailStatus === 'SENT';
  const isRejectedByBulkhead = response.status === 201 && body.emailStatus === 'EMAIL_DEFERRED';
  const expected = check(response, {
    'order created with sent or bulkhead-deferred email': () => isSent || isRejectedByBulkhead,
  });

  if (isSent) {
    sentEmails.add(1);
  }

  if (isRejectedByBulkhead) {
    bulkheadRejectedEmails.add(1);
  }

  if (!expected) {
    console.warn(`Unexpected response status=${response.status} emailStatus=${body.emailStatus || 'n/a'}`);
  }

  unexpectedResponses.add(!expected);
  sleep(0.1);
}
