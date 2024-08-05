import json
from jobdata import JobData

from scraper_utils import extract_domain, fetch_html, extract_description, extract_json_data, USER_AGENT


# scrap nofluffjobs.pl
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


# scrap pracuj.pl
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

            attributes = data.get('attributes', {})
            if attributes:
                job_data.company_name = attributes.get('displayEmployerName', job_data.company_name)

            for section in sections:
                section_type = section.get('sectionType', '')
                if section_type == 'technologies-expected':
                    job_data.technologies_expected.extend(section.get('textElements', []))
                elif section_type == 'requirements-expected':
                    job_data.requirements_expected.extend(section.get('textElements', []))

        return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)

    except Exception as e:
        return f"Exception: {e}"


def handle_scrapers(url):
    domain = extract_domain(url)
    match domain:
        case 'pracuj':
            return json.loads(scrap_pracuj(url))
        case 'nofluffjobs':
            return json.loads(scrap_nofluffjobs(url))
        case _:
            return f"Scrape for {domain} not found."
