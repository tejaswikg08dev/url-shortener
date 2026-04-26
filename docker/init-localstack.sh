#!/bin/bash

echo "Creating DynamoDB table: url_mappings..."
awslocal dynamodb create-table \
  --table-name url_mappings \
  --attribute-definitions AttributeName=short_key,AttributeType=S \
  --key-schema AttributeName=short_key,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

awslocal dynamodb update-time-to-live \
  --table-name url_mappings \
  --time-to-live-specification Enabled-true, AttributeName=expires_at

echo "Creating SQS queues..."
awslocal sqs create-queue --queue-name click-events-dlq
awslocal sqs create-queue \
  --queue-name click-events-queue \
  --attributes '{
  "RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:000000000000:click-events-dlq\", \"maxReceiveCount\" : \"3\"}",
  "VisibilityTimeout" : "30"
  }'

echo "LocalStack initialization complete!"