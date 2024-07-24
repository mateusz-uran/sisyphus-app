import requests
from bs4 import BeautifulSoup
import json
from jobdata import JobData

USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' \
             'AppleWebKit/537.36 (KHTML, like Gecko) ' \
             'Chrome/91.0.4472.124 Safari/537.36'


# Helper function to fetch HTML content from a URL
def fetch_html(url, headers):
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    response.encoding = 'utf-8'
    return response.text


# Helper function to extract JSON data from HTML
def extract_json_data(html, script_id):
    soup = BeautifulSoup(html, 'html.parser')
    script_tag = soup.find('script', id=script_id)
    if not script_tag:
        raise ValueError(f"Script tag with id '{script_id}' not found")
    return json.loads(script_tag.string)


# Helper function to extract and format list items from HTML description
def extract_description(description_html):
    description_soup = BeautifulSoup(description_html, 'html.parser')
    return [item.get_text(strip=True) for item in description_soup.find_all('li')]


# Function to scrap nofluffjobs.pl
def scrap_nofluffjobs(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, 'serverApp-state')

    posting_data = json_data.get('POSTING', {})
    company_name = posting_data.get('company', {}).get('name', "")
    requirements_must = [req['value'] for req in posting_data.get('requirements', {}).get('musts', [])]
    description_html = posting_data.get('requirements', {}).get('description', "")
    requirements_nice = extract_description(description_html)

    job_data = JobData()
    job_data.requirements_expected.extend(requirements_nice)
    job_data.technologies_expected.extend(requirements_must)
    job_data.company_name = company_name

    return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)


# Function to scrap pracuj.pl
def scrap_pracuj(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, '__NEXT_DATA__')

    try:
        text_sections = (
            json_data
            .get('props', {})
            .get('pageProps', {})
            .get('dehydratedState', {})
            .get('queries', [])
        )

        job_data = JobData()

        for query in text_sections:
            state = query.get('state', {})
            data = state.get('data', {})
            sections = data.get('textSections', [])

            for section in sections:
                section_type = section.get('sectionType', '')
                if section_type == 'technologies-expected':
                    job_data.technologies_expected.extend(section.get('textElements', []))
                elif section_type == 'requirements-expected':
                    job_data.requirements_expected.extend(section.get('textElements', []))
                elif section_type == 'about-us-description':
                    plain_text = section.get('plainText', '')
                    job_data.company_name = plain_text.split(',', 1)[0]

        return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)

    except Exception as e:
        return f"Exception: {e}"
