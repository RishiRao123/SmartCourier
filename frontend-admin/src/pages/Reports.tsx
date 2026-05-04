import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Download, Search, FileText, ChevronLeft, ChevronRight } from "lucide-react";
import toast from "react-hot-toast";
import api from "../services/api";
import StatusBadge from "../components/StatusBadge";
import CustomDatePicker from "../components/CustomDatePicker";
import LoadingSpinner from "../components/LoadingSpinner";

interface DeliveryReport {
  trackingNumber: string;
  senderName: string;
  receiverName: string;
  status: string;
  receiverAddress?: {
    city?: string;
  };
}

const Reports = () => {
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [reportData, setReportData] = useState<DeliveryReport[]>([]);
  const [loading, setLoading] = useState(false);

  // Pagination State
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const handleGenerateReport = async () => {
    if (!startDate || !endDate) {
      toast.error("Please select both start and end dates.");
      return;
    }

    setLoading(true);
    try {
      const startFormatted = `${startDate}T00:00:00Z`;
      const endFormatted = `${endDate}T23:59:59Z`;

      const response = await api.get(
        `/admin/dashboard/deliveries/report?start=${startFormatted}&end=${endFormatted}`,
      );
      const data =
        response?.data?.data !== undefined
          ? response.data.data
          : response?.data;

      if (Array.isArray(data)) {
        setReportData(data);
        setCurrentPage(1); // Reset page on new data
        toast.success(`Generated report with ${data.length} records`);
      } else {
        setReportData([]);
      }
    } catch (err: any) {
      console.error(err);
      toast.error("Failed to generate report.");
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadCSV = () => {
    if (reportData.length === 0) {
      toast.error("No data to export.");
      return;
    }

    const headers = ["Tracking ID", "Sender", "Receiver", "Status", "City"];
    const csvRows = [
      headers.join(","),
      ...reportData.map((row) =>
        [
          row.trackingNumber,
          `"${row.senderName}"`,
          `"${row.receiverName}"`,
          row.status,
          `"${row.receiverAddress?.city || "N/A"}"`,
        ].join(","),
      ),
    ];

    const csvString = csvRows.join("\n");
    const blob = new Blob([csvString], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute(
      "download",
      `SmartCourier_Report_${startDate}_to_${endDate}.csv`,
    );
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    toast.success("Report exported to CSV");
  };

  // Pagination Logic
  const totalPages = Math.ceil(reportData.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = reportData.slice(indexOfFirstItem, indexOfLastItem);

  const paginate = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    
    <div className='space-y-6 animate-fade-in-up'>
      <div className='bg-white rounded-[40px] shadow-2xl border border-gray-100 p-8'>
        <div className='flex items-center justify-between mb-8 pb-6 border-b border-gray-100'>
          <div className='flex items-center gap-4'>
            <div className='p-3 bg-blue-50 text-blue-600 rounded-2xl shadow-sm border border-blue-100'>
              <FileText className='w-8 h-8' />
            </div>
            <div>
              <h2 className='text-3xl font-black text-blue-900 tracking-tighter'>
                Reports
              </h2>
              <p className='text-gray-500 font-bold'>
                Choose dates to see and download reports.
              </p>
            </div>
          </div>
          <button
            onClick={handleDownloadCSV}
            className='flex items-center gap-2 bg-white hover:bg-gray-50 text-blue-900 border-2 border-gray-100 font-black py-3 px-6 rounded-2xl shadow-sm transition-all hover:-translate-y-0.5 active:translate-y-0'
          >
            <Download className='w-5 h-5' /> Export CSV
          </button>
        </div>

        <div className='flex flex-col lg:flex-row items-end gap-6 mb-10 bg-gray-50/50 p-6 rounded-[32px] border border-gray-100'>
          <div className='flex-1 w-full'>
            <CustomDatePicker
              label='Start Date'
              value={startDate}
              onChange={setStartDate}
              placeholder='Select start date'
            />
          </div>
          <div className='flex-1 w-full'>
            <CustomDatePicker
              label='End Date'
              value={endDate}
              onChange={setEndDate}
              placeholder='Select end date'
            />
          </div>
          <button
            onClick={handleGenerateReport}
            disabled={loading}
            className='w-full lg:w-auto flex items-center justify-center gap-3 bg-yellow-500 hover:bg-yellow-400 text-[#071a2a] font-black py-4 px-10 rounded-2xl transition-all shadow-xl disabled:opacity-70 group'
          >
            {loading ? (
              <Search className='w-6 h-6 animate-spin' />
            ) : (
              <Search className='w-6 h-6 group-hover:scale-110 transition-transform' />
            )}
            <span className='uppercase tracking-widest text-xs'>
              Generate Report
            </span>
          </button>
        </div>

        {/* Report Table */}
        <div className='bg-white rounded-[40px] shadow-2xl border border-gray-100 overflow-hidden'>
          <div className='overflow-x-auto'>
            <table className='w-full text-left border-collapse'>
              <thead>
                <tr className='bg-gray-50/50 border-b border-gray-100'>
                  <th className='px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                    Tracking ID
                  </th>
                  <th className='px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                    Shipment Info
                  </th>
                  <th className='px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                    Status
                  </th>
                  <th className='px-8 py-5 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                    City
                  </th>
                  <th className='px-8 py-5 text-right pr-12 text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]'>
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className='divide-y divide-gray-50'>
              {reportData.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className='px-6 py-12 text-center text-gray-500 text-sm font-medium'
                  >
                    No data available. Generate a report to see results.
                  </td>
                </tr>
              ) : (
                currentItems.map((row, idx) => (
                  <tr
                    key={idx}
                    onClick={() => navigate(`/tracking/${row.trackingNumber}`)}
                    className='hover:bg-blue-50/40 transition-all cursor-pointer group'
                  >
                    <td className='px-8 py-6'>
                      <span className='text-xl font-black text-blue-900 group-hover:text-yellow-500 transition-colors'>
                        #{row.trackingNumber}
                      </span>
                    </td>
                    <td className='px-8 py-6'>
                      <div className='flex flex-col gap-1'>
                        <p className='text-sm font-black text-blue-900'>
                          {row.senderName}{" "}
                          <span className='text-yellow-500 mx-1'>→</span>{" "}
                          {row.receiverName}
                        </p>
                      </div>
                    </td>
                    <td className='px-8 py-6'>
                      <StatusBadge status={row.status} />
                    </td>
                    <td className='px-8 py-6 text-sm font-bold text-gray-500'>
                      {row.receiverAddress?.city || "N/A"}
                    </td>
                    <td className='px-8 py-6 text-right pr-12'>
                      <button className='p-3 bg-white text-blue-900 rounded-2xl shadow-sm border border-gray-100 group-hover:bg-yellow-500 group-hover:border-yellow-500 group-hover:text-blue-900 transition-all'>
                        <ChevronRight className='w-5 h-5' />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="p-8 mt-4 border-t border-gray-50 flex justify-center bg-gray-50/30 rounded-b-xl">
            <div className="flex items-center gap-2">
              <button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`w-10 h-10 rounded-xl font-black text-sm transition-all ${
                    currentPage === number 
                      ? 'bg-yellow-500 text-[#071a2a] shadow-lg shadow-yellow-500/30' 
                      : 'bg-white text-gray-400 hover:bg-gray-100 border border-gray-200'
                  }`}
                >
                  {number}
                </button>
              ))}
              <button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="p-2 hover:bg-white rounded-lg disabled:opacity-30 transition-all text-[#071a2a]"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  </div>
);
};

export default Reports;
