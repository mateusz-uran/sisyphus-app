class JobData:
    def __init__(self):
        self.technologies_expected = []
        self.requirements_expected = []
        self.company_name = ""

    def to_dict(self):
        return {
            'technologies_expected': self.technologies_expected,
            'requirements_expected': self.requirements_expected,
            'company_name': self.company_name
        }
