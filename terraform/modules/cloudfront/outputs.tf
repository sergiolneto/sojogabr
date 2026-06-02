# terraform/modules/cloudfront/outputs.tf

output "cloudfront_domain_name" {
  description = "The domain name of the CloudFront distribution"
  value       = aws_cloudfront_distribution.main.domain_name
}

output "cloudfront_hosted_zone_id" {
  description = "The hosted zone ID for CloudFront (always Z2FDTNDATAQYW2)"
  value       = aws_cloudfront_distribution.main.hosted_zone_id
}
