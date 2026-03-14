import requests

# 测试获取用户列表
url = 'http://localhost:8080/admin/users?page=1&size=10'

print('测试获取用户列表...')
print(f'请求URL: {url}')

try:
    response = requests.get(url)
    print(f'响应状态码: {response.status_code}')
    print(f'响应内容: {response.text}')
    
    if response.status_code == 200:
        data = response.json()
        print(f'响应数据: {data}')
        print(f'记录数: {len(data.get("data", {}).get("records", []))}')
        print(f'总数: {data.get("data", {}).get("total", 0)}')
    else:
        print(f'请求失败: {response.status_code}')
except Exception as e:
    print(f'错误: {e}')
