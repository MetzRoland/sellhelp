export function formatDate(dateString: string): string {
  const date = new Date(dateString);

  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  const HH = String(date.getHours()).padStart(2, "0");
  const MM = String(date.getMinutes()).padStart(2, "0");

  return `${yyyy}.${mm}.${dd} ${HH}:${MM}`;
}

export function getPostStatusName(postStatusName: string): string {
  const statusNames: Record<string, string> = {
    new: "Elérhető",
    accepted: "Elvállalva",
    started: "Folyamatban",
    completed_by_employee: "Ellenőrzésre vár",
    unsuccessful_result_closed: "Sikertelenül lezárva",
    work_rejected: "Munka elutasítva",
    closed: "Kész",
  };

  return statusNames[postStatusName];
}