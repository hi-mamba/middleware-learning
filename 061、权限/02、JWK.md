
# JWK


<https://mkjwk.org/>

jose4j 创建  Public and Private Keypair

``java
RsaJsonWebKey rsaJwk = RsaJwkGenerator.generateJwk(2048);
rsaJwk.setKeyId("id");
String jwksJson= rsaJwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
``


创建jwt

```java
 JwtClaims claims=new JwtClaims();
        //过期时间
        NumericDate date=NumericDate.now();
        //有效期 
        date.addSeconds(60);
        claims.setExpirationTime(date);
        claims.setSubject("user login");
        claims.setClaim("user_id",user_id);
        JsonWebSignature jws=new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        RsaJsonWebKey rsaJsonWebKey= new RsaJsonWebKey(JsonUtil.readValue2Map(jwksJson,String.class,Object.class));
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        String jwk = jws.getCompactSerialization();
        
```
