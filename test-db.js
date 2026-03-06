const mysql = require('mysql2');

// 创建数据库连接
const connection = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: '66776677',
  database: 'tutoringsys_db'
});

// 连接数据库
connection.connect((err) => {
  if (err) {
    console.error('数据库连接失败:', err);
    return;
  }
  console.log('数据库连接成功');
  
  // 检查user表是否存在
  connection.query('SHOW TABLES', (err, results) => {
    if (err) {
      console.error('查询表失败:', err);
      connection.end();
      return;
    }
    console.log('数据库表:', results.map(row => Object.values(row)[0]));
    
    // 检查user表数据
    connection.query('SELECT * FROM user', (err, results) => {
      if (err) {
        console.error('查询用户数据失败:', err);
        connection.end();
        return;
      }
      console.log('用户数据:', results);
      connection.end();
    });
  });
});
