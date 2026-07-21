const ONES = [
  "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
  "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
  "Seventeen", "Eighteen", "Nineteen",
];
const TENS = ["", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"];

function twoDigit(n) {
  if (n < 20) return ONES[n];
  return `${TENS[Math.floor(n / 10)]} ${ONES[n % 10]}`.trim();
}

// Mirrors backend.com.service.AmountToWordsUtil - used only for the live
// on-screen preview. The server recalculates and stores its own value,
// which is what actually gets printed on the PDF.
export function amountToWords(amount) {
  let rupees = Math.floor(Number(amount) || 0);
  if (rupees === 0) return "Zero";

  let result = "";
  const crore = Math.floor(rupees / 10000000);
  rupees %= 10000000;
  const lakh = Math.floor(rupees / 100000);
  rupees %= 100000;
  const thousand = Math.floor(rupees / 1000);
  rupees %= 1000;
  const hundred = Math.floor(rupees / 100);
  const remainder = rupees % 100;

  if (crore > 0) result += `${twoDigit(crore)} Crore `;
  if (lakh > 0) result += `${twoDigit(lakh)} Lakh `;
  if (thousand > 0) result += `${twoDigit(thousand)} Thousand `;
  if (hundred > 0) result += `${ONES[hundred]} Hundred `;
  if (remainder > 0) result += twoDigit(remainder);

  return result.trim();
}
