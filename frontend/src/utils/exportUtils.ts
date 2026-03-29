import jsPDF from 'jspdf';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

export interface ErrorQuestion {
  id: number;
  questionContent: string;
  correctAnswer: string;
  studentAnswer: string;
  analysis: string;
  courseName: string;
  assignmentTitle: string;
  createTime: string;
}

export const exportToPDF = (
  questions: ErrorQuestion[],
  filename: string = '错题本',
) => {
  const doc = new jsPDF();

  doc.setFontSize(18);
  doc.text('错题本导出', 14, 22);

  doc.setFontSize(10);
  doc.text(`导出时间: ${new Date().toLocaleString()}`, 14, 30);
  doc.text(`共 ${questions.length} 道错题`, 14, 36);

  let yPos = 45;

  questions.forEach((q, index) => {
    if (yPos > 250) {
      doc.addPage();
      yPos = 20;
    }

    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text(
      `第 ${index + 1} 题 (${q.courseName} - ${q.assignmentTitle})`,
      14,
      yPos,
    );
    yPos += 8;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');

    const questionLines = doc.splitTextToSize(
      `题目: ${q.questionContent}`,
      180,
    );
    doc.text(questionLines, 14, yPos);
    yPos += questionLines.length * 5 + 3;

    doc.setTextColor(220, 53, 69);
    doc.text(`你的答案: ${q.studentAnswer}`, 14, yPos);
    yPos += 6;

    doc.setTextColor(40, 167, 69);
    doc.text(`正确答案: ${q.correctAnswer}`, 14, yPos);
    yPos += 6;

    doc.setTextColor(0, 0, 0);
    const analysisLines = doc.splitTextToSize(
      `解析: ${q.analysis || '暂无解析'}`,
      180,
    );
    doc.text(analysisLines, 14, yPos);
    yPos += analysisLines.length * 5 + 10;
  });

  doc.save(`${filename}.pdf`);
};

export const exportToExcel = (
  questions: ErrorQuestion[],
  filename: string = '错题本',
) => {
  const worksheetData = [
    [
      '序号',
      '课程',
      '作业',
      '题目内容',
      '你的答案',
      '正确答案',
      '解析',
      '错误时间',
    ],
  ];

  questions.forEach((q, index) => {
    worksheetData.push([
      String(index + 1),
      q.courseName,
      q.assignmentTitle,
      q.questionContent.replace(/<[^>]+>/g, ''),
      q.studentAnswer,
      q.correctAnswer,
      q.analysis || '暂无解析',
      q.createTime,
    ]);
  });

  const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

  worksheet['!cols'] = [
    { wch: 6 },
    { wch: 15 },
    { wch: 20 },
    { wch: 40 },
    { wch: 15 },
    { wch: 15 },
    { wch: 30 },
    { wch: 20 },
  ];

  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '错题本');

  const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  saveAs(blob, `${filename}.xlsx`);
};

export const exportErrorBook = {
  toPDF: exportToPDF,
  toExcel: exportToExcel,
};
