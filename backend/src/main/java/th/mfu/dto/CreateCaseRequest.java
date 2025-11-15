        package th.mfu.dto;

        import th.mfu.model.caseentity.CaseType;

        public class CreateCaseRequest {

            private CaseType caseType;  
            private String note;
            private Long locationId;    // ใช้แทน lat/lon

            public CaseType getCaseType() {
                return caseType;
            }

            public void setCaseType(CaseType caseType) {
                this.caseType = caseType;
            }

            public String getNote() {
                return note;
            }

            public void setNote(String note) {
                this.note = note;
            }

            public Long getLocationId() {
                return locationId;
            }

            public void setLocationId(Long locationId) {
                this.locationId = locationId;
            }
        }
