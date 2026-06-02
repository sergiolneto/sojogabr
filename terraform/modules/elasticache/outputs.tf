# terraform/modules/elasticache/outputs.tf

output "redis_endpoint" {
  description = "The DNS name of the cache cluster"
  value       = aws_elasticache_cluster.main.cache_nodes[0].address
}
