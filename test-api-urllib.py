import urllib.request
import urllib.parse
import json

# 测试获取用户列表
url = 'http://localhost:8080/admin/users'
params = {
    'page': 1,
    'size': 10
}

# 构建URL
url_with_params = url + '?' + urllib.parse.urlencode(params)

print('测试获取用户列表...')
print(f'请求URL: {url_with_params}')

try:
    # 发起请求
    with urllib.request.urlopen(url_with_params) as response:
        # 读取响应
        data = response.read()
        # 解码响应
        text = data.decode('utf-8')
        print(f'响应状态码: {response.status}')
        print(f'响应内容: {text}')
        
        # 解析JSON
        if text:
            json_data = json.loads(text)
            print(f'响应数据: {json_data}')
            records = json_data.get('data', {}).get('records', [])
            print(f'记录数: {len(records)}')
            print(f'总数: {json_data.get("data", {}).get("total", 0)}')
            # 打印每个用户的信息
            for i, user in enumerate(records):
                print(f'用户 {i+1}: {user.get("username")}, {user.get("realName")}, {user.get("role")}')
except Exception as e:
    print(f'错误: {e}')
