# Plant-Cultivation API 测试文档

## 1. 认证模块 (Auth)

### 1.1 用户注册

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**失败响应 - 用户名已存在 (200)：**
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

---

### 1.2 用户登录

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**失败响应 - 用户名或密码错误 (200)：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 2. 植物模块 (Plant)

### 2.1 获取植物列表

```http
GET http://localhost:8080/api/plants
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "绿萝",
      "nameEn": "Pothos",
      "slug": "lv-luo",
      "scientificName": "Epipremnum aureum",
      "description": "绿萝是一种常见的室内观叶植物...",
      "imageUrl": "/uploads/xxx.jpg",
      "categoryName": "观叶植物"
    }
  ]
}
```

---

### 2.2 获取植物详情

```http
GET http://localhost:8080/api/plants/{slug}
```

**示例：**
```http
GET http://localhost:8080/api/plants/lv-luo
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "绿萝",
    "nameEn": "Pothos",
    "slug": "lv-luo",
    "scientificName": "Epipremnum aureum",
    "family": "天南星科",
    "origin": "东南亚热带雨林",
    "description": "绿萝是一种常见的室内观叶植物...",
    "lightRequirement": "散射光",
    "waterFrequency": "每周1-2次",
    "temperature": "15-30℃",
    "humidity": "60-80%",
    "soilType": "疏松透气的腐叶土",
    "fertilizer": "每月1次稀薄液肥",
    "difficulty": "简单",
    "toxicity": "对猫狗有毒",
    "imageUrl": "/uploads/xxx.jpg",
    "images": ["/uploads/xxx1.jpg", "/uploads/xxx2.jpg"],
    "careGuides": [
      {
        "season": "春季",
        "tips": "增加浇水频率...",
        "wateringAdvice": "土壤干燥时浇透"
      }
    ],
    "categoryName": "观叶植物"
  }
}
```

**失败响应 - 植物不存在 (200)：**
```json
{
  "code": 404,
  "message": "植物不存在",
  "data": null
}
```

---

## 3. 分类模块 (Category)

### 3.1 获取所有分类

```http
GET http://localhost:8080/api/categories
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "观叶植物",
      "slug": "guanye",
      "description": "以观叶为主的植物"
    },
    {
      "id": 2,
      "name": "多肉植物",
      "slug": "duorou",
      "description": "肉质植物"
    }
  ]
}
```

---

### 3.2 获取分类详情

```http
GET http://localhost:8080/api/categories/{id}
```

**示例：**
```http
GET http://localhost:8080/api/categories/1
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "观叶植物",
    "slug": "guanye",
    "description": "以观叶为主的植物"
  }
}
```

---

### 3.3 获取分类下的植物

```http
GET http://localhost:8080/api/categories/{id}/plants
```

**示例：**
```http
GET http://localhost:8080/api/categories/1/plants
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "绿萝",
      "slug": "lv-luo",
      "imageUrl": "/uploads/xxx.jpg"
    }
  ]
}
```

---

## 4. 帖子模块 (Post)

### 4.1 获取帖子列表

```http
GET http://localhost:8080/api/posts
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "title": "我的绿萝养护心得",
      "content": "分享一下我养绿萝的经验...",
      "imageUrl": "/uploads/xxx.jpg",
      "likeCount": 42,
      "commentCount": 5,
      "authorName": "testuser",
      "createdAt": "2024-06-15T10:30:00"
    }
  ]
}
```

---

### 4.2 获取帖子详情

```http
GET http://localhost:8080/api/posts/{id}
```

**示例：**
```http
GET http://localhost:8080/api/posts/1
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "title": "我的绿萝养护心得",
    "content": "分享一下我养绿萝的经验...",
    "imageUrl": "/uploads/xxx.jpg",
    "likeCount": 42,
    "commentCount": 5,
    "authorName": "testuser",
    "authorAvatar": "/uploads/avatar.jpg",
    "createdAt": "2024-06-15T10:30:00",
    "comments": [
      {
        "id": 1,
        "content": "写得很好！",
        "authorName": "user2",
        "createdAt": "2024-06-15T12:00:00"
      }
    ]
  }
}
```

---

### 4.3 发布帖子 🔒

```http
POST http://localhost:8080/api/posts
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "我的绿萝养护心得",
  "content": "分享一下我养绿萝的经验...",
  "imageUrl": "/uploads/xxx.jpg"
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1
  }
}
```

**失败响应 - 未登录 (200)：**
```json
{
  "code": 401,
  "message": "未登录或登录已过期",
  "data": null
}
```

---

### 4.4 点赞帖子 🔒

```http
POST http://localhost:8080/api/posts/{id}/like
Authorization: Bearer {token}
```

**示例：**
```http
POST http://localhost:8080/api/posts/1/like
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

**取消点赞 (再次调用)：**
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

---

### 4.5 获取帖子评论

```http
GET http://localhost:8080/api/posts/{id}/comments
```

**示例：**
```http
GET http://localhost:8080/api/posts/1/comments
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "content": "写得很好！",
      "authorName": "user2",
      "authorAvatar": "/uploads/avatar.jpg",
      "createdAt": "2024-06-15T12:00:00"
    }
  ]
}
```

---

### 4.6 发表评论 🔒

```http
POST http://localhost:8080/api/posts/{id}/comments
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "写得很好！"
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 1
  }
}
```

---

## 5. 植物日记模块 (PlantDiary)

### 5.1 获取用户日记列表 🔒

```http
GET http://localhost:8080/api/diaries
Authorization: Bearer {token}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "plantName": "绿萝",
      "content": "今天给绿萝浇了水，叶子更绿了",
      "imageUrl": "/uploads/xxx.jpg",
      "height": 30.5,
      "leafCount": 15,
      "createdAt": "2024-06-15T10:30:00"
    }
  ]
}
```

---

### 5.2 创建日记 🔒

```http
POST http://localhost:8080/api/diaries
Content-Type: application/json
Authorization: Bearer {token}

{
  "plantId": 1,
  "content": "今天给绿萝浇了水，叶子更绿了",
  "imageUrl": "/uploads/xxx.jpg",
  "height": 30.5,
  "leafCount": 15
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

---

### 5.3 获取日记详情 🔒

```http
GET http://localhost:8080/api/diaries/{id}
Authorization: Bearer {token}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "plantName": "绿萝",
    "content": "今天给绿萝浇了水，叶子更绿了",
    "imageUrl": "/uploads/xxx.jpg",
    "height": 30.5,
    "leafCount": 15,
    "createdAt": "2024-06-15T10:30:00"
  }
}
```

---

### 5.4 更新日记 🔒

```http
PUT http://localhost:8080/api/diaries/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "更新：叶子长得更好了",
  "height": 32.0,
  "leafCount": 18
}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.5 删除日记 🔒

```http
DELETE http://localhost:8080/api/diaries/{id}
Authorization: Bearer {token}
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 6. 图片上传模块 (Upload)

### 6.1 上传图片 🔒

```http
POST http://localhost:8080/api/upload/image
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: [选择图片文件]
```

**成功响应 (200)：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "/uploads/abc123.jpg"
}
```

**失败响应 - 文件过大 (200)：**
```json
{
  "code": 400,
  "message": "文件大小不能超过10MB",
  "data": null
}
```

**失败响应 - 文件类型错误 (200)：**
```json
{
  "code": 400,
  "message": "只支持图片文件",
  "data": null
}
```

---

## 7. SPA 路由模块

### 7.1 获取前端页面

```http
GET http://localhost:8080/
GET http://localhost:8080/plants
GET http://localhost:8080/posts
```

返回对应的 HTML 页面（Vue SPA）。

---

## 测试说明

### 🔒 标记说明

- 🔒 表示需要登录认证的接口
- 测试需要认证的接口时，需要在 Header 中添加：
  ```
  Authorization: Bearer {token}
  ```

### 测试流程

1. **先调用注册接口** 创建测试用户
2. **调用登录接口** 获取 token
3. **使用 token** 测试需要认证的接口

### 使用 Postman 测试

1. 将登录返回的 token 保存到环境变量：
   ```
   {{token}}: eyJhbGciOiJIUzI1NiJ9...
   ```

2. 在需要认证的请求 Header 中添加：
   ```
   Authorization: Bearer {{token}}
   ```

### 使用 curl 测试

```bash
# 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 获取植物列表
curl http://localhost:8080/api/plants

# 发布帖子（需要 token）
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"title":"测试帖子","content":"内容"}'
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功（业务成功或失败） |
| 400 | 参数错误 |
| 401 | 未登录或 token 无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 状态码映射

后端使用 `code` 字段表示业务状态：

| code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

文档生成时间：2026-06-28
